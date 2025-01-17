package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.util.UIUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

@Component
public class MainViewController implements Initializable {
	@FXML
	private GridPane tickerGrid;
	
	@FXML
	private Button loadTickers;
	
	@FXML
	private Button secondPage;
	
	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	
	@Autowired
	public MainViewController(UserSettingsService userSettingsService, UIUtils uiUtils) {
		this.userSettingsService = userSettingsService;
		this.uiUtils = uiUtils;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		try {
			UserSettings settings = this.userSettingsService.getUserSettings().get();
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
