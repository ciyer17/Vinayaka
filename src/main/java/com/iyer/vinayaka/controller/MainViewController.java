package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.entities.UserTickers;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class MainViewController implements Initializable {
	@FXML private AnchorPane mainAnchorPane;
	@FXML private ImageView settingsImageView;
	@FXML private ScrollPane tickerScrollPane;
	
	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	private final DataHolder dataHolder;
	private final UserTickersService userTickersService;
	
	@Autowired
	public MainViewController(UserSettingsService userSettingsService, UserTickersService service, UIUtils uiUtils, DataHolder dataHolder) {
		this.userSettingsService = userSettingsService;
		this.userTickersService = service;
		this.uiUtils = uiUtils;
		this.dataHolder = dataHolder;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			UserSettings settings = this.userSettingsService.getUserSettings().get();
			this.dataHolder.setUserSettings(settings);
			
			this.setBackground(settings.getDark_mode());
			List<UserTickers> tickers = this.userTickersService.getAllTickersSortedBySymbol();
			
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
			
			List<UserTickers> sampleTickers = List.of(
					appleTicker, nvidiaTicker, lucidTicker, palantirTicker,
					googleTicker, vugTicker, gamestopTicker, sofiTicker,
					oracleTicker, microsoftTicker
			);
			
			this.populateGrid(sampleTickers);
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
	
	private void populateGrid(List<UserTickers> tickers) {
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
		
		int row = 0;
		int col = 0;
		
		for (UserTickers ticker : tickers) {
			Label tickerNameLabel = new Label(ticker.getSymbol());
			Label tickerPriceLabel = new Label(ticker.getName());
			Label tickerChangeLabel = new Label(ticker.getExchange());
			VBox tickerBox = new VBox(tickerNameLabel, tickerPriceLabel, tickerChangeLabel);
			tickerBox.setAlignment(Pos.CENTER);
			VBox.setMargin(tickerBox, new Insets(5, 5, 5, 5));
			tickerBox.getChildren().stream().map(Label.class::cast).forEach(label -> {
				label.setPadding(new Insets(0, 0, 5, 0));
				// TODO: Later change the style according to price change.
				label.getStylesheets().removeAll("tickerPositive", "tickerNegative", "tickerZero");
				label.getStyleClass().add("tickerZero");
			});
			// tickerBox.getChildren().forEach(Node::applyCss);
			tickerGrid.add(tickerBox, col, row);
			
			col++;
			if (col == MAX_COLUMNS) {
				col = 0;
				row++;
				RowConstraints constraints = new RowConstraints();
				constraints.setVgrow(Priority.ALWAYS);
				constraints.setMinHeight(100);
				tickerGrid.getRowConstraints().add(constraints);
			}
		}
		
		this.tickerScrollPane.setContent(tickerGrid);
	}
}
