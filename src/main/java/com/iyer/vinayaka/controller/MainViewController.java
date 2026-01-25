package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.entities.UserTickers;
import com.iyer.vinayaka.service.AlpacaMarketDataService;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.service.UserTickersService;
import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.TickerRefresher;
import com.iyer.vinayaka.util.UIUtils;
import jakarta.annotation.PostConstruct;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MainViewController implements Initializable {
	@FXML private AnchorPane mainAnchorPane;
	@FXML private ImageView settingsImageView;
	@FXML private ScrollPane tickerScrollPane;
	@FXML private TextField searchTickerTextField;
	@FXML private ImageView searchTickerButton;
	@FXML private HBox searchHbox;

	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	private final DataHolder dataHolder;
	private final UserTickersService userTickersService;
	private final AlpacaMarketDataService alpacaMarketDataService;
	private final List<Assets> assets;
	private final ApplicationContext context;
	private TickerRefresher tickerRefresher;

	// Cached UI components for reuse - avoids recreation on every refresh
	private GridPane tickerGrid;
	private static final int MAX_COLUMNS = 3;

	// Cached images loaded once at startup - avoids repeated resource loading
	private Image deleteIconImage;
	private Image favoriteFilledImage;
	private Image favoriteOutlineImage;

	// Map of ticker symbol -> its UI components for in-place updates
	private final Map<String, TickerUIComponents> tickerUICache = new HashMap<>();

	/**
	 * Holds references to all UI components for a single ticker.
	 * Enables in-place updates without recreating nodes.
	 */
	private static class TickerUIComponents {
		final StackPane container;
		final Label nameLabel;
		final Label priceLabel;
		final Label changeLabel;
		final ImageView deleteIcon;
		final ImageView favoriteIcon;
		final VBox infoBox;

		TickerUIComponents(StackPane container, Label nameLabel, Label priceLabel,
				Label changeLabel, ImageView deleteIcon, ImageView favoriteIcon, VBox infoBox) {
			this.container = container;
			this.nameLabel = nameLabel;
			this.priceLabel = priceLabel;
			this.changeLabel = changeLabel;
			this.deleteIcon = deleteIcon;
			this.favoriteIcon = favoriteIcon;
			this.infoBox = infoBox;
		}
	}

	public MainViewController(UserSettingsService userSettingsService, UserTickersService service,
							  UIUtils uiUtils, DataHolder dataHolder, AlpacaMarketDataService alpacaMarketDataService,
							  ApplicationContext context) {
		this.userSettingsService = userSettingsService;
		this.userTickersService = service;
		this.uiUtils = uiUtils;
		this.dataHolder = dataHolder;
		this.alpacaMarketDataService = alpacaMarketDataService;
		this.context = context;
		this.assets = this.alpacaMarketDataService.getAllAssets();
	}

	/**
	 * Initializes the TickerRefresher bean. This has to be done separately to prevent a circular dependency situation.
	 */
	@PostConstruct
	public void initTickerRefresher() {
		this.tickerRefresher = this.context.getBean(TickerRefresher.class);
	}

	/**
	 * Initializes the MainViewController. Handles window resizing for elements, sets the background of the application,
	 * fetches the user's settings, and starts the ticker refresher.
	 * @param url
	 * @param resourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		// Initialize cached images at startup
		this.initializeCachedImages();

		// Initialize GridPane once - reused on every refresh
		this.tickerGrid = this.createTickerGrid();
		this.tickerScrollPane.setContent(this.tickerGrid);

		// Handle window resizing for elements
		this.mainAnchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
			// The subtraction of half the width of the searchHbox is necessary to center the Hbox.
			double width = this.mainAnchorPane.getWidth() / 2 - (this.searchHbox.getPrefWidth() / 2);
			AnchorPane.setLeftAnchor(this.searchHbox, width);
		});

		try {
			UserSettings settings = this.userSettingsService.getUserSettings().get();
			this.dataHolder.setUserSettings(settings);

			this.setBackground(settings.getDark_mode());
			List<UserTickers> tickers = this.userTickersService.getAllTickersWithFavoritesFirst();
			this.fetchInfoAndPopulate(tickers);

			Platform.runLater(() -> {
				this.tickerRefresher.startRefresh();
			});

		} catch (NoSuchElementException n) {
			// runLater() defers execution until after MainView is fully initialized. This prevents an
			// overwrite where MainView is rendered, then APISecrets is rendered, then MainView is rendered again.
			Platform.runLater(() -> {
				this.uiUtils.navigateToSpecifiedPage(UIUtils.ENTER_API_SECRETS_VIEW, this.getClass());
			});
		}
	}

	/**
	 * Loads and caches icon images at startup.
	 */
	private void initializeCachedImages() {
		this.deleteIconImage = new Image(Objects.requireNonNull(
				getClass().getResourceAsStream(UIUtils.DELETE_ICON)));
		this.favoriteFilledImage = new Image(Objects.requireNonNull(
				getClass().getResourceAsStream(UIUtils.FAVORITE_ICON_FILLED)));
		this.favoriteOutlineImage = new Image(Objects.requireNonNull(
				getClass().getResourceAsStream(UIUtils.FAVORITE_ICON_OUTLINE)));
	}

	/**
	 * Creates and configures the ticker GridPane with column constraints.
	 * Called once at initialization; the GridPane is reused on every refresh.
	 *
	 * @return A configured GridPane ready for ticker content.
	 */
	private GridPane createTickerGrid() {
		GridPane grid = new GridPane(10, 10);
		grid.setAlignment(Pos.CENTER);
		grid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		grid.setMaxHeight(Region.USE_COMPUTED_SIZE);
		grid.setHgap(50);
		grid.setVgap(50);

		for (int i = 0; i < MAX_COLUMNS; i++) {
			ColumnConstraints column = new ColumnConstraints();
			column.setPercentWidth(100.00 / MAX_COLUMNS);
			column.setHgrow(Priority.ALWAYS);
			grid.getColumnConstraints().add(column);
		}

		return grid;
	}

	/**
	 * Navigates to the settings page.
	 *
	 * @param event The mouse event that triggered the navigation.
	 */
	public void navigateToSettingsPage(MouseEvent event) {
		this.uiUtils.navigateToSpecifiedPage(UIUtils.UPDATE_SETTINGS_VIEW, this.getClass());
	}

	/**
	 * Searches for a ticker symbol and adds it to the user's list of tickers if it exists.
	 *
	 * @param event The mouse event that triggered the search.
	 */
	public void searchTicker(MouseEvent event) {
		String tickerSymbol = this.searchTickerTextField.getText().toUpperCase();
		if (tickerSymbol.isBlank()) {
			this.uiUtils.showAlert("No ticker entered", "Please enter a ticker symbol to search for.", Alert.AlertType.ERROR);
		} else {
			// Determine if the searched ticker symbol is a valid ticker whose information is available on Alpaca Markets
			boolean tickerExists = this.assets.parallelStream().anyMatch(asset -> asset.getSymbol().equals(tickerSymbol));
			if (tickerExists) {
				boolean tickerAlreadyAdded = this.userTickersService.getAllTickersWithFavoritesFirst().parallelStream()
						.anyMatch(ticker -> ticker.getSymbol().equals(tickerSymbol));
				if (!tickerAlreadyAdded) {
					Map<String, String> nameAndExchange = this.alpacaMarketDataService.getTickerNameAndExchange(tickerSymbol);
					UserTickers ticker = new UserTickers(tickerSymbol,nameAndExchange.get("officialName"),
							nameAndExchange.get("listedExchange"), false);
					this.userTickersService.addTicker(ticker);

					// Invalidate the ticker cache since we've added a new ticker
					this.tickerRefresher.invalidateTickerCache();
					this.fetchInfoAndPopulate(this.userTickersService.getAllTickersWithFavoritesFirst());
				} else {
					this.uiUtils.showAlert("Ticker Already Added",
							"You have already added " + "\"" + tickerSymbol + "\"!", Alert.AlertType.INFORMATION);
				}
			} else {
				this.uiUtils.showAlert("Ticker Not Found",
						"The ticker symbol " + "\"" + tickerSymbol + "\"" + " was not found.", Alert.AlertType.ERROR);
			}
		}
	}

	/**
	 * Updates the ticker grid UI with current price data using in-place updates
	 * when possible.
	 *
	 * <p>
	 * <b>OPTIMIZATION STRATEGY:</b> This method uses a caching strategy to
	 * dramatically reduce
	 * object creation during periodic refreshes. Instead of destroying and
	 * recreating all UI nodes
	 * on every refresh (which happened every 10 seconds by default), this method:
	 * </p>
	 * <ol>
	 * <li>Reuses the same GridPane instance (created once at initialization)</li>
	 * <li>Maintains a cache (tickerUICache) mapping each ticker symbol to its UI
	 * components</li>
	 * <li>For existing tickers: updates only the text and styles of existing
	 * Labels</li>
	 * <li>For new tickers: creates UI components and adds them to the cache</li>
	 * <li>For removed tickers: removes components from both grid and cache</li>
	 * </ol>
	 *
	 * <p>
	 * Each ticker is displayed with:
	 * </p>
	 * <ul>
	 * <li>Ticker symbol</li>
	 * <li>Current price (from latest bar's close price)</li>
	 * <li>Price change percentage</li>
	 * <li>Color coding (green for positive, red for negative, neutral for
	 * zero)</li>
	 * <li>Delete icon (visible on hover)</li>
	 * <li>Favorite icon (visible on hover, filled if favorited)</li>
	 * </ul>
	 *
	 * <p>
	 * <b>Layout:</b> Tickers are arranged in a 3-column grid, with favorites first
	 * then non-favorites (both sorted alphabetically).
	 * </p>
	 *
	 * @param latestBars  Map of ticker symbols to their latest stock bars (1-minute
	 *                    bars).
	 *                    Each list should contain at least one bar.
	 * @param priceChange Map of ticker symbols to their price change percentages
	 *                    compared
	 *                    to the previous trading day's close. Values are already
	 *                    rounded to 2 decimals.
	 * @param tickers     The list of tickers in display order (favorites first,
	 *                    then alphabetical).
	 *                    This is passed in from fetchInfoAndPopulate() to avoid a
	 *                    redundant database query
	 *                    that was previously happening on every refresh.
	 */
	private void populateGrid(Map<String, List<StockBar>> latestBars, Map<String, Double> priceChange,
			List<UserTickers> tickers) {

		// Build a lookup map from the ticker list
		// Purpose: Convert the List<UserTickers> into a Map for O(1) lookups by symbol.
		// Why this is important: We need to quickly access ticker metadata (like
		// isFavorite)
		// when processing each ticker. Using a Map avoids O(n) list searches.
		// Note: The tickers list is passed in from fetchInfoAndPopulate(), which
		// already
		// fetched it from the database. Previously, this method called
		// getAllTickersWithFavoritesFirst()
		// again, causing a redundant database query on every refresh cycle.
		Map<String, UserTickers> tickerMap = tickers.stream()
				.collect(Collectors.toMap(UserTickers::getSymbol, ticker -> ticker));

		// Identify and remove tickers that no longer exist
		// Purpose: Clean up the cache and grid when a user deletes a ticker.
		// Why this is important: Without this cleanup, deleted tickers would remain in
		// the cache
		// forever, causing memory leaks and potentially showing stale data if the
		// ticker is re-added.
		// Process:
		// 1. Get all symbols currently in our UI cache (these are tickers we're
		// displaying)
		// 2. Remove from that set all symbols that are in the current ticker list
		// 3. What remains are symbols that were deleted and need cleanup
		Set<String> currentSymbols = tickerMap.keySet();
		Set<String> removedSymbols = new HashSet<>(this.tickerUICache.keySet());
		removedSymbols.removeAll(currentSymbols);

		// For each removed ticker, clean up its UI components from both the cache and
		// the grid.
		// This ensures we don't have orphaned UI components or memory leaks.
		for (String symbol : removedSymbols) {
			TickerUIComponents removed = this.tickerUICache.remove(symbol);
			if (removed != null) {
				// Remove the StackPane container from the GridPane's children list.
				// This is necessary because the GridPane still holds a reference to it.
				this.tickerGrid.getChildren().remove(removed.container);
			}
		}

		// Update existing or create new UI components for each ticker
		// Purpose: Reuse existing UI components when possible.
		// Why this is important: Creating JavaFX nodes (Labels, VBoxes, StackPanes) is
		// expensive.
		// Each node creation involves:
		// - Memory allocation for the node object
		// - CSS styling computation
		// - Scene graph integration
		// - Event handler setup
		// By reusing nodes, we avoid all of this overhead on every refresh.
		for (UserTickers ticker : tickers) {
			String tickerSymbol = ticker.getSymbol();

			// Extract the price data for this ticker from the API response maps.
			// These maps were populated by fetchInfoAndPopulate() from the Alpaca API.
			List<StockBar> bars = latestBars.get(tickerSymbol);

			// Safe access: If no bar data exists (e.g., market is closed or API issue),
			// default to 0.0 to prevent NullPointerException or NoSuchElementException.
			Double latestTradePrice = (bars != null && !bars.isEmpty()) ? bars.getLast().getC() : 0.0;

			// getOrDefault prevents NPE if the ticker wasn't in the price change map for
			// any reason.
			Double priceChangePercentage = priceChange.getOrDefault(tickerSymbol, 0.0);

			// Check if we already have UI components cached for this ticker
			TickerUIComponents components = this.tickerUICache.get(tickerSymbol);

			if (components != null) {
				// =========================================================================
				// FAST PATH: IN-PLACE UPDATE (most common case during periodic refresh)
				// =========================================================================
				// Purpose: Update only the dynamic data (price, change %, styles) without
				// touching the node structure.
				// Why this is important: During normal operation, most tickers already
				// exist in the cache. Updating a Label's text is essentially
				// just changing a String reference - orders of magnitude faster than creating
				// a new Label, adding it to a VBox, wrapping in a StackPane, etc.
				updateTickerLabels(components, latestTradePrice, priceChangePercentage);

				// Also update the favorite icon in case the user toggled it.
				// This swaps the Image reference if needed, but reuses the same ImageView.
				updateFavoriteIcon(components.favoriteIcon, ticker.isFavorite());
			} else {
				// =========================================================================
				// SLOW PATH: CREATE NEW COMPONENTS (only when a new ticker is added)
				// =========================================================================
				// Purpose: Create a complete set of UI components for a ticker that doesn't
				// exist in our cache yet (user just added it).
				// Why this is important: New tickers need full UI setup - we can't avoid this.
				// However, this only happens when the user explicitly adds a ticker, not on
				// every refresh cycle. So the cost is acceptable.
				components = createTickerUIComponents(ticker, latestTradePrice, priceChangePercentage);

				// Add to cache so future refreshes can use the fast path above.
				// The cache key is the ticker symbol (e.g., "AAPL") for O(1) lookups.
				this.tickerUICache.put(tickerSymbol, components);
			}
		}

		// Rebuild the Grid layout to ensure correct display order
		// =====================================================================================
		// Purpose: Position all ticker containers in the grid in the correct order.
		// Why this is important: The order matters for user experience - favorites
		// should
		// appear first, followed by non-favorites. When a user toggles a favorite, the
		// ticker
		// should move to its new position.
		// Note: This clears and re-adds children to the GridPane, but it's NOT
		// recreating the
		// children themselves - it's just changing their position in the grid. The
		// actual
		// StackPane, Label, etc. objects remain the same (identity preserved).
		rebuildGridLayout(tickers);
	}

	/**
	 * Updates the text and style classes for an existing ticker's Labels.
	 * We simply update the text property of existing ones.
	 *
	 * <b>Why this is efficient:</b> Updating Label.setText() is essentially just
	 * changing a String reference internally. JavaFX handles the minimal repaint
	 * needed.
	 *
	 * @param components            The cached UI components for the ticker
	 *                              (contains Label references).
	 * @param price                 The current price to display (from the latest
	 *                              API response).
	 * @param priceChangePercentage The price change percentage (positive, negative,
	 *                              or zero).
	 */
	private void updateTickerLabels(TickerUIComponents components, Double price, Double priceChangePercentage) {
		// Update the price label with the new value.
		// The ticker name label doesn't change (it's always the symbol), so we skip it.
		components.priceLabel.setText(Double.toString(price));

		// Update the change percentage label (e.g., "2.5%" or "-1.3%")
		components.changeLabel.setText(priceChangePercentage + "%");

		// Update the CSS style classes for all three labels to reflect the new price
		// direction.
		// All labels in a ticker box share the same color (green for up, red for down,
		// neutral for flat).
		updateLabelStyleClass(components.nameLabel, priceChangePercentage);
		updateLabelStyleClass(components.priceLabel, priceChangePercentage);
		updateLabelStyleClass(components.changeLabel, priceChangePercentage);
	}

	/**
	 * Updates the CSS style class of a Label based on whether the price change is
	 * positive, negative, or zero.
	 *
	 * @param label                 The Label to update (could be name, price, or
	 *                              change label).
	 * @param priceChangePercentage The price change percentage used to determine
	 *                              which style to apply.
	 */
	private void updateLabelStyleClass(Label label, Double priceChangePercentage) {
		// Remove any existing price-direction style classes.
		// This is necessary because the price direction can change between refreshes
		label.getStyleClass().removeAll("tickerPositive", "tickerNegative", "tickerZero");

		// Apply the appropriate style class based on the price change direction.
		// These classes are defined in the application's CSS stylesheet and control the
		// text color.
		if (priceChangePercentage > 0) {
			label.getStyleClass().add("tickerPositive");
		} else if (priceChangePercentage < 0) {
			label.getStyleClass().add("tickerNegative");
		} else {
			label.getStyleClass().add("tickerZero");
		}
	}

	/**
	 * Updates the favorite icon's image based on the current favorite status.
	 *
	 * @param favoriteIcon The ImageView to update.
	 * @param isFavorite   Whether this ticker is currently marked as a favorite.
	 */
	private void updateFavoriteIcon(ImageView favoriteIcon, boolean isFavorite) {
		// Select the appropriate cached image based on favorite status
		Image targetImage = isFavorite ? this.favoriteFilledImage : this.favoriteOutlineImage;

		// Only update if the image reference is different.
		if (favoriteIcon.getImage() != targetImage) {
			favoriteIcon.setImage(targetImage);
		}
	}

	/**
	 * Creates a complete set of UI components for a new ticker. Called only when a
	 * user
	 * adds a new ticker that doesn't exist in our cache. Not called during normal
	 * periodic
	 * refreshes.
	 *
	 * <p>
	 * <b>What it creates:</b>
	 * </p>
	 * <ul>
	 * <li>Three Labels (name, price, change percentage)</li>
	 * <li>VBox to hold the labels vertically</li>
	 * <li>Delete icon ImageView (using cached image)</li>
	 * <li>Favorite icon ImageView (using cached image)</li>
	 * <li>StackPane container to layer everything together</li>
	 * </ul>
	 *
	 * @param ticker                The UserTickers entity with symbol and favorite
	 *                              status.
	 * @param price                 The current price to display.
	 * @param priceChangePercentage The price change percentage.
	 * @return A TickerUIComponents object containing references to all created UI
	 *         nodes.
	 */
	private TickerUIComponents createTickerUIComponents(UserTickers ticker, Double price,
			Double priceChangePercentage) {
		String tickerSymbol = ticker.getSymbol();

		// Create the three labels that display ticker information.
		Label tickerNameLabel = new Label(tickerSymbol);
		Label tickerPriceLabel = new Label(Double.toString(price));
		Label tickerChangeLabel = new Label(priceChangePercentage + "%");

		// Create a VBox to stack the labels vertically (symbol on top, then price, then
		// change %)
		VBox tickerInfoBox = new VBox(tickerNameLabel, tickerPriceLabel, tickerChangeLabel);
		tickerInfoBox.setAlignment(Pos.CENTER);

		// Apply padding and initial style classes to all labels.
		// The style class determines the color based on price direction.
		for (Label label : List.of(tickerNameLabel, tickerPriceLabel, tickerChangeLabel)) {
			label.setPadding(new Insets(0, 0, 5, 0));
			updateLabelStyleClass(label, priceChangePercentage);
		}

		// Create the delete and favorite icons.
		ImageView deleteIcon = createDeleteIcon(tickerSymbol);
		ImageView favoriteIcon = createFavoriteIcon(ticker);

		// Create a StackPane to layer the components
		StackPane tickerContainer = new StackPane(tickerInfoBox, deleteIcon, favoriteIcon);

		// Position the icons at the corners of the container
		StackPane.setAlignment(deleteIcon, Pos.BOTTOM_RIGHT);
		StackPane.setAlignment(favoriteIcon, Pos.BOTTOM_LEFT);

		// Add margins to push the icons slightly inward from the container edges
		StackPane.setMargin(deleteIcon, new Insets(0, 10, 10, 0));
		StackPane.setMargin(favoriteIcon, new Insets(0, 0, 10, 10));

		// Add margin around the entire container for spacing in the grid
		StackPane.setMargin(tickerContainer, new Insets(50, 50, 50, 50));

		// Change cursor to hand on hover to indicate the container is interactive
		tickerContainer.setCursor(Cursor.HAND);

		// Set up hover handlers to show/hide the icons.
		tickerContainer.setOnMouseEntered(e -> {
			deleteIcon.setVisible(true);
			favoriteIcon.setVisible(true);
		});
		tickerContainer.setOnMouseExited(e -> {
			deleteIcon.setVisible(false);
			favoriteIcon.setVisible(false);
		});

		return new TickerUIComponents(tickerContainer, tickerNameLabel, tickerPriceLabel,
				tickerChangeLabel, deleteIcon, favoriteIcon, tickerInfoBox);
	}

	/**
	 * Rebuilds the grid layout by repositioning all ticker containers in the
	 * correct order (favorites first, then non-favorites, both alphabetically
	 * sorted
	 * within their groups).
	 *
	 * When a user toggles a ticker's favorite status, that ticker needs to move to
	 * a
	 * new position in the grid. Simply updating the labels isn't enough as we need
	 * to
	 * physically reposition the container in the GridPane.
	 *
	 * @param tickers The list of tickers in the desired display order.
	 */
	private void rebuildGridLayout(List<UserTickers> tickers) {
		// Clear all children from the grid. This removes the node references from the
		// GridPane's internal child list, but does NOT destroy the nodes themselves.
		// The nodes are still referenced by our tickerUICache and will be re-added
		// below.
		this.tickerGrid.getChildren().clear();

		// Clear row constraints so we can rebuild them based on how many rows we need.
		// Row constraints control the height behavior of each row.
		this.tickerGrid.getRowConstraints().clear();

		// Track our position in the grid as we add tickers
		int col = 0;
		int row = 0;

		// Add each ticker's container to the grid in order.
		// The tickers list is already sorted (favorites first, then alphabetically) by
		// the caller.
		for (UserTickers ticker : tickers) {
			// Get the cached UI components for this ticker
			TickerUIComponents components = this.tickerUICache.get(ticker.getSymbol());

			if (components != null) {
				// Add the container to the grid at the current position.
				// GridPane.add(node, column, row) places the node at the specified cell.
				this.tickerGrid.add(components.container, col, row);

				// Move to the next column
				col++;

				// If we've filled all columns in this row, move to the next row
				if (col == MAX_COLUMNS) {
					col = 0;
					row++;

					// Add row constraints for the new row.
					// This ensures each row has a minimum height and can grow if needed.
					RowConstraints constraints = new RowConstraints();
					constraints.setVgrow(Priority.ALWAYS); // Allow row to grow to fill available space
					constraints.setMinHeight(100); // Ensure minimum height for readability
					this.tickerGrid.getRowConstraints().add(constraints);
				}
			}
		}
	}

	/**
	 * Creates a delete icon ImageView for the specified ticker symbol.
	 *
	 * @param tickerSymbol The symbol of the ticker this delete icon is associated
	 *                     with.
	 * @return An ImageView configured as a delete button.
	 */
	private ImageView createDeleteIcon(String tickerSymbol) {
		// Use the cached image instead of loading from resources.
		ImageView deleteIcon = new ImageView(this.deleteIconImage);

		// Configure the icon's display properties
		deleteIcon.setFitWidth(UIUtils.ICON_SIZE);
		deleteIcon.setFitHeight(UIUtils.ICON_SIZE);
		deleteIcon.setPreserveRatio(true);

		// Use the full bounds as the hit target for reliable clicks.
		// Without this, clicks on transparent pixels would not register.
		deleteIcon.setPickOnBounds(true);

		// Start hidden - will be shown on hover via the container's mouse handlers
		deleteIcon.setVisible(false);

		// Visual feedback that this is clickable
		deleteIcon.setCursor(Cursor.HAND);

		// Set up the click handler to delete the ticker
		deleteIcon.setOnMouseClicked(event -> {
			handleDeleteTicker(tickerSymbol, event);
			event.consume(); // Prevent event propagation to parent containers
		});

		return deleteIcon;
	}

	/**
	 * Creates a favorite icon ImageView for the specified ticker.
	 *
	 * @param ticker The ticker this favorite icon is associated with.
	 * @return An ImageView configured as a favorite toggle button.
	 */
	private ImageView createFavoriteIcon(UserTickers ticker) {
		// Select the appropriate cached image based on the ticker's current favorite
		// status.
		Image favoriteImage = ticker.isFavorite() ? this.favoriteFilledImage : this.favoriteOutlineImage;

		// Create a new ImageView using the cached Image
		ImageView favoriteIcon = new ImageView(favoriteImage);

		// Configure the icon's display properties
		favoriteIcon.setFitWidth(UIUtils.ICON_SIZE);
		favoriteIcon.setFitHeight(UIUtils.ICON_SIZE);
		favoriteIcon.setPreserveRatio(true);

		// Use bounds hit-testing because the outline icon has transparent pixels.
		// Without this, clicks would only register on the visible heart outline pixels,
		// making it frustrating to click the non-favorite state.
		favoriteIcon.setPickOnBounds(true);

		// Start hidden - will be shown on hover via the container's mouse handlers
		favoriteIcon.setVisible(false);

		// Visual feedback that this is clickable
		favoriteIcon.setCursor(Cursor.HAND);

		// Set up the click handler to toggle favorite status
		favoriteIcon.setOnMouseClicked(event -> {
			handleToggleFavorite(ticker.getSymbol(), event);
			event.consume(); // Prevent event propagation to parent containers
		});

		return favoriteIcon;
	}

	/**
	 *
	 * Handles the deletion of a ticker. Shows a confirmation dialog, and if the
	 * user confirms, animates the ticker fading out, then removes it from the
	 * database and
	 * refreshes the grid.
	 *
	 * @param tickerSymbol The symbol of the ticker to delete.
	 * @param event        The mouse event that triggered the deletion (used to find
	 *                     the parent
	 *                     container).
	 */
	private void handleDeleteTicker(String tickerSymbol, MouseEvent event) {
		// Show confirmation dialog
		boolean confirmed = this.uiUtils.showConfirmationDialog(
			"Delete Ticker",
			"Are you sure you want to delete " + tickerSymbol + "?"
		);

		if (!confirmed) {
			return;
		}

		// Find the StackPane container
		ImageView deleteIcon = (ImageView) event.getSource();
		StackPane tickerContainer = (StackPane) deleteIcon.getParent();

		// Create fade-out animation
		FadeTransition fadeOut = new FadeTransition(Duration.millis(200), tickerContainer);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);

		// Delete from database and refresh grid after animation completes
		fadeOut.setOnFinished(e -> {
			this.userTickersService.deleteTicker(tickerSymbol);
			// Invalidate the ticker cache since we've deleted a ticker
			this.tickerRefresher.invalidateTickerCache();
			List<UserTickers> updatedTickers = this.userTickersService.getAllTickersWithFavoritesFirst();
			this.fetchInfoAndPopulate(updatedTickers);
		});

		fadeOut.play();
	}

	/**
	 * Handles toggling the favorite status of a ticker.
	 * Updates the database and refreshes the grid with a brief pulse animation.
	 *
	 * @param tickerSymbol The symbol of the ticker to toggle.
	 * @param event        The mouse event that triggered the toggle.
	 */
	private void handleToggleFavorite(String tickerSymbol, MouseEvent event) {
		// Get the icon
		ImageView favoriteIcon = (ImageView) event.getSource();

		// Keep icon visible during the operation to prevent hover interference
		favoriteIcon.setVisible(true);

		Tooltip tooltip = new Tooltip("");

		// Toggle in database immediately
		if (this.userTickersService.toggleFavorite(tickerSymbol).isFavorite()) {
			tooltip.setText("Unfavorite");
		} else {
			tooltip.setText("Favorite");
		}
		Tooltip.install(favoriteIcon, tooltip);

		// Brief pulse animation for visual feedback
		FadeTransition pulse = new FadeTransition(Duration.millis(100), favoriteIcon);
		pulse.setFromValue(1.0); // Start fully visible (100% opacity).
		pulse.setToValue(0.3); // Fade down to 30% opacity at the lowest point.
		pulse.setCycleCount(1); // Run the fade down + up twice total.
		pulse.setAutoReverse(true); // Reverse at the end of each cycle (fade back up).

		// Refresh grid after quick animation
		pulse.setOnFinished(e -> {
			// Invalidate the ticker cache since the favorite order has changed
			this.tickerRefresher.invalidateTickerCache();
			List<UserTickers> updatedTickers = this.userTickersService.getAllTickersWithFavoritesFirst();
			this.fetchInfoAndPopulate(updatedTickers);
		});

		pulse.play();
	}

	/**
	 * Sets the background of the application based on the dark mode setting.
	 *
	 * @param darkMode The dark mode setting.
	 */
	private void setBackground(boolean darkMode) {
		Image darkCog = new Image(Objects.requireNonNull(getClass().
				getResourceAsStream(UIUtils.DARK_MODE_IMG)));
		Image lightCog = new Image(Objects.requireNonNull(getClass().
				getResourceAsStream(UIUtils.LIGHT_MODE_IMG)));
		Image searchTicker = new Image(Objects.requireNonNull(getClass().
				getResourceAsStream(UIUtils.SEARCH_TICKER)));

		if (darkMode) {
			this.settingsImageView.setImage(darkCog);
			this.mainAnchorPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.mainAnchorPane.getStyleClass().add(UIUtils.DARK_MODE_BG);
			this.tickerScrollPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().add(UIUtils.DARK_MODE_BG);
			this.searchTickerTextField.getStyleClass().removeAll(UIUtils.DARK_MODE_TEXTFIELD, UIUtils.LIGHT_MODE_TEXTFIELD);
			this.searchTickerTextField.getStyleClass().add(UIUtils.DARK_MODE_TEXTFIELD);
		} else {
			this.settingsImageView.setImage(lightCog);
			this.mainAnchorPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.mainAnchorPane.getStyleClass().add(UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().add(UIUtils.LIGHT_MODE_BG);
			this.searchTickerTextField.getStyleClass().add(UIUtils.LIGHT_MODE_TEXTFIELD);
		}
		this.searchTickerButton.setImage(searchTicker);
	}

	/**
	 * Fetches the latest market data for the given tickers and updates the UI grid.
	 *
	 * <p>
	 * This method is the central point for updating ticker information in the UI.
	 * It's called by:
	 * <ul>
	 * <li>The periodic refresh mechanism ({@link TickerRefresher})</li>
	 * <li>User actions (adding, deleting, or favoriting tickers)</li>
	 * <li>Initial application startup</li>
	 * </ul>
	 *
	 * <p>
	 * <b>Data Fetching:</b> Retrieves the latest stock bars and price change
	 * percentages from
	 * {@link AlpacaMarketDataService}, to calculate changes compared to the
	 * previous trading day.
	 * </p>
	 *
	 * @param tickers The list of tickers to fetch data for and display in the grid.
	 *                Should be sorted with favorites first for proper display
	 *                order.
	 *
	 * @see TickerRefresher#startRefresh()
	 * @see AlpacaMarketDataService#getPriceChangePercentages(List)
	 * @see #populateGrid(Map, Map, List)
	 */
	public void fetchInfoAndPopulate(List<UserTickers> tickers) {
		if (!tickers.isEmpty()) {
			// Fetch the latest price data from Alpaca Markets API.
			// This returns a list containing:
			// - Element 0: Map<String, List<StockBar>> - latest stock bars for each ticker
			// - Element 1: Map<String, Double> - price change percentages for each ticker
			List<Map<String, ?>> priceChangeAndTrades = this.alpacaMarketDataService.
					getPriceChangePercentages((tickers.stream().map(UserTickers::getSymbol).toList()));

			// Check if market data was successfully fetched before accessing elements.
			// The API might return empty results if the market is closed or there's an
			// error.
			if (priceChangeAndTrades.isEmpty() || priceChangeAndTrades.size() < 2) {
				return;
			}

			// Extract the two maps from the API response.
			// We use @SuppressWarnings because we know the exact types returned by the
			// service, but the compiler can't verify generic types at runtime.
			@SuppressWarnings("unchecked")
			Map<String, List<StockBar>> latestStockBars = (Map<String, List<StockBar>>) priceChangeAndTrades.getFirst();
			@SuppressWarnings("unchecked")
			Map<String, Double> priceChange = (Map<String, Double>) priceChangeAndTrades.getLast();

			// Schedule the UI update on the JavaFX Application Thread.
			// This is critical because:
			// 1. This method is often called from TickerRefresher's background executor
			// thread
			// 2. JavaFX requires all UI modifications to happen on the Application Thread
			// 3. Platform.runLater() queues the update to run on the correct thread
			Platform.runLater(() -> this.populateGrid(latestStockBars, priceChange, tickers));
		}
	}
}
