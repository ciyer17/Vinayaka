package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.UIUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class MainViewController implements Initializable {
	@FXML private GridPane tickerGrid;
	@FXML private Button loadTickers;
	@FXML private ImageView settingsImageView;
	
	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	private final DataHolder dataHolder;
	
	@Autowired
	public MainViewController(UserSettingsService userSettingsService, UIUtils uiUtils, DataHolder dataHolder) {
		this.userSettingsService = userSettingsService;
		this.uiUtils = uiUtils;
		this.dataHolder = dataHolder;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			UserSettings settings = this.userSettingsService.getUserSettings().get();
			this.dataHolder.setUserSettings(settings);
			
			Image darkMode = new Image(Objects.requireNonNull(getClass().
					getResourceAsStream("/icons/settings-dark-mode.png")));
			Image lightMode = new Image(Objects.requireNonNull(getClass().
					getResourceAsStream("/icons/settings-light-mode.png")));
			
			System.out.println("Loading dark mode image: " + getClass().getResource("/icons/settings-dark-mode.png"));
			System.out.println("Loading light mode image: " + getClass().getResource("/icons/settings-light-mode.png"));
			
			if (settings.getDark_mode()) {
				this.settingsImageView.setImage(darkMode);
			} else {
				this.settingsImageView.setImage(lightMode);
			}
			// Process
			
		} catch (NoSuchElementException n) {
			// runLater() defers execution until after MainView is fully initialized. This prevents an
			// overwrite where MainView is rendered, then APISecrets is rendered, then MainView is rendered again.
			Platform.runLater(() -> {
				this.uiUtils.navigateToSpecifiedPage("/view/APISecrets.fxml", this.getClass());
			});
		}
		
		// TODO:
		
		String[][] tickers = {
				{"AAPL", "NASDAQ", "Apple Inc."},
				{"GOOGL", "NASDAQ", "Alphabet Inc."},
				{"TSLA", "NASDAQ", "Tesla Inc."},
				{"AMZN", "NASDAQ", "Amazon Inc."},
				{"MSFT", "NASDAQ", "Microsoft Corp."}
		};
		loadTickers.setOnAction(e -> populateGrid(tickers));
	}
	
	public void navigateToAPISecretsPage(ActionEvent event) {
		this.uiUtils.navigateToSpecifiedPage("/view/APISecrets.fxml", this.getClass());
	}
	
	public void navigateToSettingsPage(MouseEvent event) {
		this.uiUtils.navigateToSpecifiedPage("/view/UpdateSettings.fxml", this.getClass());
	}
	
	private void populateGrid(String[][] tickers) {
		
		// Iterate through tickers and add them to the grid
		int row = 0;
		int col = 0;
		
		for (String[] ticker : tickers) {
			// Create a simple label for demonstration (replace with custom nodes if needed)
			Label tickerLabel = new Label(ticker[0] + " (" + ticker[1] + "): " + ticker[2]);
			tickerGrid.add(tickerLabel, col, row);
			
			// Move to the next column or row
			col++;
			if (col == 3) { // 3 columns per row
				col = 0;
				row++;
			}
		}
	}
}
