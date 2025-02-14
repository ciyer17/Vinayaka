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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	@Autowired
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
			List<UserTickers> tickers = this.userTickersService.getAllTickersSortedBySymbol();
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
				boolean tickerAlreadyAdded = this.userTickersService.getAllTickersSortedBySymbol().parallelStream()
						.anyMatch(ticker -> ticker.getSymbol().equals(tickerSymbol));
				if (!tickerAlreadyAdded) {
					Map<String, String> nameAndExchange = this.alpacaMarketDataService.getTickerNameAndExchange(tickerSymbol);
					UserTickers ticker = new UserTickers(tickerSymbol,nameAndExchange.get("officialName"),
							nameAndExchange.get("listedExchange"), false);
					this.userTickersService.addTicker(ticker);
					
					this.fetchInfoAndPopulate(this.userTickersService.getAllTickersSortedBySymbol());
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
	 * Creates a grid of tickers, lists them in alphabetical order, and colors them based on their price change.
	 *
	 * @param latestBars The latest stock bars for each ticker, mapped to the respective ticker.
	 * @param priceChange The price change for each ticker, mapped to the respective ticker.
	 */
	private void populateGrid(Map<String, List<StockBar>> latestBars, Map<String, Double> priceChange) {
		GridPane tickerGrid = new GridPane(10,10);
		// Set up the grid
		tickerGrid.setAlignment(Pos.CENTER);
		tickerGrid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		tickerGrid.setMaxHeight(Region.USE_COMPUTED_SIZE);
		tickerGrid.getChildren().clear();
		tickerGrid.getRowConstraints().clear();
		tickerGrid.getColumnConstraints().clear();
		
		int MAX_COLUMNS = 3;
		
		for (int i = 0; i < MAX_COLUMNS; i++) {
			ColumnConstraints column = new ColumnConstraints();
			column.setPercentWidth(100.00 / MAX_COLUMNS);
			column.setHgrow(Priority.ALWAYS);
			tickerGrid.getColumnConstraints().add(column);
		}
		
		// Create a VBox for each ticker
		List<VBox> tickerBoxes = priceChange.keySet().parallelStream().sorted().map(
				tickerSymbol -> {
					Double latestTradePrice = latestBars.get(tickerSymbol).getLast().getC();
					Double priceChangePercentage = priceChange.get(tickerSymbol);
					
					Label tickerNameLabel = new Label(tickerSymbol);
					Label tickerPriceLabel = new Label(Double.toString(latestTradePrice));
					Label tickerChangeLabel = new Label(priceChangePercentage + "%");
					
					VBox tickerBox = new VBox(tickerNameLabel, tickerPriceLabel, tickerChangeLabel);
					VBox.setMargin(tickerBox, new Insets(50, 50, 50, 50));
					tickerBox.setCursor(Cursor.HAND);
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
		tickerGrid.setHgap(50);
		tickerGrid.setVgap(50);
		
		// Need to run this on the JavaFX Application Thread, as this is a UI update.
		Platform.runLater(() -> {
			this.tickerScrollPane.setContent(tickerGrid);
		});
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
	 * Fetches the latest data for the given tickers and passes them off to create the grid.
	 *
	 * @param tickers The tickers whose data is to be fetched.
	 */
	public void fetchInfoAndPopulate(List<UserTickers> tickers) {
		if (!tickers.isEmpty()) {
			List<Map<String, ?>> priceChangeAndTrades = this.alpacaMarketDataService.
					getPriceChangePercentages((tickers.stream().map(UserTickers::getSymbol).toList()));
			
			// For the below, we know for a fact that the first element is a Map<String, List<StockBar>> and the second
			// element is a Map<String, Double>. We can safely cast them to their respective types.
			@SuppressWarnings("unchecked")
			Map<String, List<StockBar>> latestStockBars = (Map<String, List<StockBar>>) priceChangeAndTrades.getFirst();
			@SuppressWarnings("unchecked")
			Map<String, Double> priceChange = (Map<String, Double>) priceChangeAndTrades.getLast();
			
			this.populateGrid(latestStockBars, priceChange);
		}
	}
}
