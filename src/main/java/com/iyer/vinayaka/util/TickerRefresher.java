package com.iyer.vinayaka.util;

import com.iyer.vinayaka.controller.MainViewController;
import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.entities.UserTickers;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.service.UserTickersService;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TickerRefresher {
	private final UserSettingsService userSettingsService;
	private final UserTickersService userTickersService;
	private final ScheduledExecutorService executor;
	private final ApplicationContext context;
	
	@Autowired
	public TickerRefresher(UserSettingsService service, UserTickersService tickersService, ApplicationContext context) {
		this.userSettingsService = service;
		this.userTickersService = tickersService;
		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.context = context;
	}
	
	@EventListener
	public void handleContextCloseEvent(ContextClosedEvent event) {
		System.out.println("Context closed. Stopping refresh.");
		this.stopRefresh();
	}
	
	public void startRefresh() {
		UserSettings settings;
		if (this.userSettingsService.getUserSettings().isPresent()) {
			settings = this.userSettingsService.getUserSettings().get();
		} else {
			throw new RuntimeException("User settings not found. Cannot start refresh.");
		}
		
		MainViewController mainViewController = this.context.getBean(MainViewController.class);
		
		int refreshInterval = settings.getRefresh_interval();
		// Need to run this on the JavaFX Application Thread to prevent the infamous
		// "Not on FX application thread; currentThread = main" error.
		Platform.runLater(() -> {
			this.executor.scheduleAtFixedRate(() -> {
				List<UserTickers> tickers = this.userTickersService.getAllTickersSortedBySymbol();
				mainViewController.fetchInfoAndPopulate(tickers);
			}, 0, refreshInterval, TimeUnit.SECONDS);
		});
	}
	
	public void stopRefresh() {
		this.executor.shutdown();
	}
}
