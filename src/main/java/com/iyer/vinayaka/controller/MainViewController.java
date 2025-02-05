package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.entities.UserTickers;
import com.iyer.vinayaka.service.AlpacaMarketDataService;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.service.UserTickersService;
import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MainViewController implements Initializable {
	@FXML private AnchorPane mainAnchorPane;
	@FXML private ImageView settingsImageView;
	@FXML private ScrollPane tickerScrollPane;
	
	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	private final DataHolder dataHolder;
	private final UserTickersService userTickersService;
	private final AlpacaMarketDataService alpacaMarketDataService;
	
	@Autowired
	public MainViewController(UserSettingsService userSettingsService, UserTickersService service,
							  UIUtils uiUtils, DataHolder dataHolder, AlpacaMarketDataService alpacaMarketDataService) {
		this.userSettingsService = userSettingsService;
		this.userTickersService = service;
		this.uiUtils = uiUtils;
		this.dataHolder = dataHolder;
		this.alpacaMarketDataService = alpacaMarketDataService;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			UserSettings settings = this.userSettingsService.getUserSettings().get();
			this.dataHolder.setUserSettings(settings);
			
			this.setBackground(settings.getDark_mode());
			List<UserTickers> tickers = this.userTickersService.getAllTickersSortedBySymbol();
			List<UserTickers> sampleTickers = getUserTickers();
			
			List<Map<String, ?>> priceChangeAndTrades = this.alpacaMarketDataService.
					getPriceChangePercentages((sampleTickers.stream().map(UserTickers::getSymbol).toList()));
			
			// For the below, we know for a fact that the first element is a Map<String, List<StockBar>> and the second
			// element is a Map<String, Double>. We can safely cast them to their respective types.
			@SuppressWarnings("unchecked")
			Map<String, List<StockBar>> latestStockBars = (Map<String, List<StockBar>>) priceChangeAndTrades.getFirst();
			@SuppressWarnings("unchecked")
			Map<String, Double> priceChange = (Map<String, Double>) priceChangeAndTrades.getLast();
			
			this.populateGrid(latestStockBars, priceChange);
			// Process
			
		} catch (NoSuchElementException n) {
			// runLater() defers execution until after MainView is fully initialized. This prevents an
			// overwrite where MainView is rendered, then APISecrets is rendered, then MainView is rendered again.
			Platform.runLater(() -> {
				this.uiUtils.navigateToSpecifiedPage(UIUtils.ENTER_API_SECRETS_VIEW, this.getClass());
			});
		}
	}
	
	public void navigateToSettingsPage(MouseEvent event) {
		this.uiUtils.navigateToSpecifiedPage(UIUtils.UPDATE_SETTINGS_VIEW, this.getClass());
	}
	
	private void setBackground(boolean darkMode) {
		Image darkCog = new Image(Objects.requireNonNull(getClass().
				getResourceAsStream(UIUtils.DARK_MODE_IMG)));
		Image lightCog = new Image(Objects.requireNonNull(getClass().
				getResourceAsStream(UIUtils.LIGHT_MODE_IMG)));
		
		if (darkMode) {
			this.settingsImageView.setImage(darkCog);
			this.mainAnchorPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.mainAnchorPane.getStyleClass().add(UIUtils.DARK_MODE_BG);
			this.tickerScrollPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().add(UIUtils.DARK_MODE_BG);
		} else {
			this.settingsImageView.setImage(lightCog);
			this.mainAnchorPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.mainAnchorPane.getStyleClass().add(UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().removeAll(UIUtils.DARK_MODE_BG, UIUtils.LIGHT_MODE_BG);
			this.tickerScrollPane.getStyleClass().add(UIUtils.LIGHT_MODE_BG);
		}
	}
	
	private void populateGrid(Map<String, List<StockBar>> latestBars, Map<String, Double> priceChange) {
		int MAX_COLUMNS = 3;
		
		GridPane tickerGrid = new GridPane(10,10);
		tickerGrid.setGridLinesVisible(true);
		tickerGrid.setAlignment(Pos.CENTER);
		tickerGrid.setCursor(Cursor.HAND);
		tickerGrid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		tickerGrid.setMaxHeight(Region.USE_COMPUTED_SIZE);
		tickerGrid.getChildren().clear();
		tickerGrid.getRowConstraints().clear();
		tickerGrid.getColumnConstraints().clear();
		
		for (int i = 0; i < MAX_COLUMNS; i++) {
			ColumnConstraints column = new ColumnConstraints();
			column.setPercentWidth(100.00 / MAX_COLUMNS);
			column.setHgrow(Priority.ALWAYS);
			tickerGrid.getColumnConstraints().add(column);
		}
		
		// Create a VBox for each ticker
		List<VBox> tickerBoxes = priceChange.entrySet().stream().map(
				entry -> {
					String tickerSymbol = entry.getKey();
					Double latestTradePrice = latestBars.get(tickerSymbol).getLast().getC();
					Double priceChangePercentage = priceChange.get(tickerSymbol);
					
					Label tickerNameLabel = new Label(tickerSymbol);
					Label tickerPriceLabel = new Label(Double.toString(latestTradePrice));
					Label tickerChangeLabel = new Label(Double.toString(priceChangePercentage) + "%");
					
					VBox tickerBox = new VBox(tickerNameLabel, tickerPriceLabel, tickerChangeLabel);
					VBox.setMargin(tickerBox, new Insets(50, 50, 50, 50));
					tickerBox.setAlignment(Pos.CENTER);
					tickerBox.getChildren().stream().map(Label.class::cast).forEach(label -> {
						label.setPadding(new Insets(0, 0, 5, 0));
						label.getStylesheets().removeAll("tickerPositive", "tickerNegative", "tickerZero");
						if (priceChangePercentage > 0) {
							label.getStyleClass().add("tickerPositive");
						} else if (priceChangePercentage < 0) {
							label.getStyleClass().add("tickerNegative");
						} else {
							label.getStyleClass().add("tickerZero");
						}
					});
					
					return tickerBox;
				}
		).toList(); // Collect all the VBoxes into a list.
		
		// AtomicInteger is required for lambda expressions, as they require final or effectively final variables.
		AtomicInteger col = new AtomicInteger(0);
		AtomicInteger row = new AtomicInteger(0);
		
		// Add each of the VBoxes to the GridPane
		tickerBoxes.forEach(tickerBox -> {
			tickerGrid.add(tickerBox, col.get(), row.get());
			col.incrementAndGet();
			if (col.get() == MAX_COLUMNS) {
				col.set(0);
				row.incrementAndGet();
				RowConstraints constraints = new RowConstraints();
				constraints.setVgrow(Priority.ALWAYS);
				constraints.setMinHeight(100);
				tickerGrid.getRowConstraints().add(constraints);
			}
		});
		this.tickerScrollPane.setContent(tickerGrid);
	}
	
	private static List<UserTickers> getUserTickers() {
		UserTickers appleTicker = new UserTickers();
		appleTicker.setExchange("NASDAQ");
		appleTicker.setName("Apple");
		appleTicker.setFavorite(false);
		appleTicker.setSymbol("AAPL");
		
		UserTickers nvidiaTicker = new UserTickers();
		nvidiaTicker.setExchange("NASDAQ");
		nvidiaTicker.setName("NVIDIA");
		nvidiaTicker.setFavorite(false);
		nvidiaTicker.setSymbol("NVDA");
		
		UserTickers lucidTicker = new UserTickers();
		lucidTicker.setExchange("NYSE");
		lucidTicker.setName("Lucid");
		lucidTicker.setFavorite(false);
		lucidTicker.setSymbol("LCID");
		
		UserTickers palantirTicker = new UserTickers();
		palantirTicker.setExchange("NASDAQ");
		palantirTicker.setName("Palantir");
		palantirTicker.setFavorite(false);
		palantirTicker.setSymbol("PLTR");
		
		UserTickers googleTicker = new UserTickers();
		googleTicker.setExchange("NASDAQ");
		googleTicker.setName("Google");
		googleTicker.setFavorite(false);
		googleTicker.setSymbol("GOOGL");
		
		UserTickers vugTicker = new UserTickers();
		vugTicker.setExchange("NYSE");
		vugTicker.setName("Vanguard Growth ETF");
		vugTicker.setFavorite(false);
		vugTicker.setSymbol("VUG");
		
		UserTickers gamestopTicker = new UserTickers();
		gamestopTicker.setExchange("NYSE");
		gamestopTicker.setName("Gamestop");
		gamestopTicker.setFavorite(false);
		gamestopTicker.setSymbol("GME");
		
		UserTickers sofiTicker = new UserTickers();
		sofiTicker.setExchange("NASDAQ");
		sofiTicker.setName("SoFi");
		sofiTicker.setFavorite(false);
		sofiTicker.setSymbol("SOFI");
		
		UserTickers oracleTicker = new UserTickers();
		oracleTicker.setExchange("NYSE");
		oracleTicker.setName("Oracle");
		oracleTicker.setFavorite(false);
		oracleTicker.setSymbol("ORCL");
		
		UserTickers microsoftTicker = new UserTickers();
		microsoftTicker.setExchange("NASDAQ");
		microsoftTicker.setName("Microsoft");
		microsoftTicker.setFavorite(false);
		microsoftTicker.setSymbol("MSFT");
		
		return List.of(
				appleTicker, nvidiaTicker, lucidTicker, palantirTicker,
				googleTicker, vugTicker, gamestopTicker, sofiTicker,
				oracleTicker, microsoftTicker
		);
	}
}
