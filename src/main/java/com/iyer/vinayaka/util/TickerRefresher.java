package com.iyer.vinayaka.util;

import com.iyer.vinayaka.controller.MainViewController;
import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.entities.UserTickers;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.service.UserTickersService;
import javafx.application.Platform;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages the periodic refresh of stock ticker data in the application.
 *
 * <p>
 * This component is responsible for scheduling and executing background tasks
 * that fetch
 * the latest ticker information from the Alpaca Markets API and update the UI.
 * It uses a
 * {@link ScheduledExecutorService} to run refresh tasks at user-configurable
 * intervals.
 * </p>
 *
 * <p>
 * <b>Threading Model:</b>
 * </p>
 * <ul>
 * <li>Refresh tasks execute on a background thread separate from the JavaFX
 * Application Thread</li>
 * <li>UI updates within the tasks use {@link Platform#runLater} to ensure
 * thread safety</li>
 * <li>The executor uses a single-threaded pool to serialize refresh
 * operations</li>
 * </ul>
 *
 * <p>
 * <b>Caching Strategy:</b>
 * </p>
 * <ul>
 * <li>Ticker lists are cached to minimize database queries during refresh
 * cycles</li>
 * <li>Cache is invalidated when tickers are added, deleted, or favorited</li>
 * <li>Price data is fetched fresh on every refresh cycle</li>
 * </ul>
 *
 * <p>
 * <b>Error Handling:</b>
 * </p>
 * <ul>
 * <li>Individual refresh cycle errors are caught and logged</li>
 * <li>The refresh mechanism continues running even if a cycle fails</li>
 * <li>Network errors or API failures do not stop future refresh attempts</li>
 * </ul>
 *
 * <p>
 * <b>Lifecycle:</b>
 * </p>
 * <ul>
 * <li>Started by {@link MainViewController} after initialization</li>
 * <li>Stopped automatically on application shutdown via
 * {@link ContextClosedEvent}</li>
 * <li>Can be manually stopped without closing the application</li>
 * </ul>
 *
 * @see MainViewController#fetchInfoAndPopulate(List)
 * @see UserSettings#getRefresh_interval()
 */
@Component
public class TickerRefresher {
	private final UserSettingsService userSettingsService;
	private final UserTickersService userTickersService;
	private final ScheduledExecutorService executor;
	private final ApplicationContext context;

	// Cache for ticker list to avoid repeated database queries on every refresh
	// cycle.
	// This cache is invalidated when tickers are added, removed, or favorited.
	private List<UserTickers> cachedTickers = null;

	/**
	 * Constructs a new TickerRefresher with the required dependencies.
	 *
	 * <p>
	 * Initializes a single-threaded scheduled executor service for managing refresh
	 * tasks.
	 * The executor ensures that only one refresh task runs at a time, preventing
	 * concurrent
	 * updates and race conditions.
	 * </p>
	 *
	 * @param service        The user settings service for retrieving refresh
	 *                       interval configuration
	 * @param tickersService The user tickers service for fetching the list of
	 *                       tracked tickers
	 * @param context        The Spring application context for accessing other
	 *                       beans (MainViewController)
	 */
	public TickerRefresher(UserSettingsService service, UserTickersService tickersService, ApplicationContext context) {
		this.userSettingsService = service;
		this.userTickersService = tickersService;
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.context = context;
	}

	/**
	 * Handles the Spring application context closing event.
	 *
	 * <p>
	 * This method is automatically invoked when the Spring application context is
	 * being closed,
	 * typically during application shutdown. It ensures that the refresh executor
	 * is properly
	 * stopped and all background tasks are terminated gracefully.
	 * </p>
	 *
	 * @param event The context closed event (not used but required by the event
	 *              listener signature)
	 */
	@EventListener
	public void handleContextCloseEvent(ContextClosedEvent event) {
		System.out.println("Context closed. Stopping refresh.");
		this.stopRefresh();
	}

	/**
	 * Invalidates the cached ticker list, forcing a fresh database query on the
	 * next refresh.
	 * This method should be called whenever the ticker list changes (add, delete,
	 * or favorite operations).
	 */
	public void invalidateTickerCache() {
		this.cachedTickers = null;
	}

	/**
	 * Gets the ticker list for refresh, using the cache if available.
	 * If the cache is empty, it queries the database and populates the cache.
	 *
	 * @return A list of UserTickers objects, sorted with favorites first.
	 */
	private List<UserTickers> getTickersForRefresh() {
		return this.cachedTickers != null ? this.cachedTickers
				: (this.cachedTickers = this.userTickersService.getAllTickersWithFavoritesFirst());
	}

	/**
	 * Starts the periodic ticker refresh process using the user's configured
	 * refresh interval.
	 *
	 * <p>
	 * This method schedules a background task that runs at a fixed rate, fetching
	 * the latest
	 * ticker data and updating the UI. The refresh task begins immediately (0
	 * delay) and continues
	 * at the interval specified in {@link UserSettings#getRefresh_interval()}.
	 * </p>
	 *
	 * <p>
	 * <b>Threading:</b> The scheduled task executes on a background thread managed
	 * by
	 * {@link ScheduledExecutorService}. UI updates within the task use
	 * {@link Platform#runLater}
	 * to ensure thread safety. The scheduling itself happens synchronously on the
	 * calling thread.
	 * </p>
	 *
	 * <p>
	 * <b>Error Handling:</b> If any exception occurs during a refresh cycle
	 * (network error,
	 * API error, database error), it is caught, logged, and the refresh continues
	 * with the next
	 * scheduled cycle. This prevents the entire refresh mechanism from stopping due
	 * to a single failure.
	 * </p>
	 *
	 * @throws RuntimeException if user settings cannot be found in the database
	 *
	 * @see #stopRefresh()
	 * @see #invalidateTickerCache()
	 * @see UserSettings#getRefresh_interval()
	 * @see MainViewController#fetchInfoAndPopulate(List)
	 */
	public void startRefresh() {
		UserSettings settings;
		if (this.userSettingsService.getUserSettings().isPresent()) {
			settings = this.userSettingsService.getUserSettings().get();
		} else {
			throw new RuntimeException("User settings not found. Cannot start refresh.");
		}

		MainViewController mainViewController = this.context.getBean(MainViewController.class);

		int refreshInterval = settings.getRefresh_interval();

		// Schedule the refresh task on a background thread. The scheduleAtFixedRate()
		// method itself doesn't need to run on the FX thread - only the UI updates
		// inside the
		// task do. fetchInfoAndPopulate() already handles FX thread management via
		// Platform.runLater().
		this.executor.scheduleAtFixedRate(() -> {
			try {
				// Get ticker list using cache to minimize database queries
				// Cache is invalidated when tickers are added/removed/favorited
				List<UserTickers> tickers = this.getTickersForRefresh();
				// Update the UI with the latest ticker data. This method handles FX thread
				// safety internally.
				mainViewController.fetchInfoAndPopulate(tickers);
			} catch (Exception e) {
				// Log the error but continue with the next refresh cycle
				// This prevents the entire refresh mechanism from stopping due to a single
				// error
				System.err.println("Error during ticker refresh: " + e.getMessage());
				e.printStackTrace();
			}
		}, 0, refreshInterval, TimeUnit.SECONDS);
	}

	/**
	 * Stops the ticker refresh process and gracefully shuts down the executor.
	 *
	 * <p>
	 * This method terminates the scheduled refresh tasks but does NOT close the
	 * application.
	 * It follows the recommended {@link ExecutorService} shutdown pattern:
	 * </p>
	 * <ol>
	 * <li>Initiates graceful shutdown with {@code shutdown()}</li>
	 * <li>Waits up to 5 seconds for tasks to complete naturally</li>
	 * <li>Forces shutdown with {@code shutdownNow()} if timeout occurs</li>
	 * <li>Waits again for tasks to respond to interruption</li>
	 * </ol>
	 *
	 * <p>
	 * <b>Important:</b> {@code shutdownNow()} sends interrupt signals but doesn't
	 * forcefully kill threads. Tasks must properly respond to interruption for
	 * clean
	 * termination.
	 * </p>
	 *
	 * <p>
	 * <b>Thread Safety:</b> This method is safe to call multiple times. Subsequent
	 * calls after the first successful shutdown will be no-ops.
	 * </p>
	 *
	 * @see #startRefresh()
	 * @see ScheduledExecutorService#shutdown()
	 * @see ScheduledExecutorService#shutdownNow()
	 */
	public void stopRefresh() {
		// Only shutdown if not already shut down
		if (!this.executor.isShutdown()) {
			this.executor.shutdown();
			try {
				// Wait up to 5 seconds for existing tasks to complete gracefully
				if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
					// If tasks didn't complete, force shutdown by sending interrupt signals
					this.executor.shutdownNow();
					// Wait again for tasks to respond to interruption (shutdownNow doesn't kill
					// threads instantly)
					if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
						System.err.println("Executor did not terminate after forced shutdown");
					}
				}
				System.out.println("Refresh stopped successfully.");
			} catch (InterruptedException e) {
				// If interrupted during shutdown, force immediate shutdown and restore
				// interrupt status
				this.executor.shutdownNow();
				Thread.currentThread().interrupt();
				System.err.println("Refresh stop interrupted: " + e.getMessage());
			}
		}
	}
}
