package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.util.UIUtils;
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
	
	@Autowired
	private UserSettingsService userSettingsService;
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		UserSettings settings = null;
		try {
			settings = this.userSettingsService.getUserSettings().get();
			// Process
			
		} catch (NoSuchElementException n) {
			// TODO: Make the UI for getting the user settings and transition on empty
			// Process
			
			// this.userSettingsService.addUserSettings(settings);
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
		UIUtils.navigateToSpecifiedPage(event,"/view/APISecrets.fxml", this.getClass());
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
