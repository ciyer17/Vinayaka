package com.iyer.vinayaka.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainViewController implements Initializable {
	@FXML
	private GridPane tickerGrid;
	
	@FXML
	private Button loadTickers;
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		String[][] tickers = {
				{"AAPL", "NASDAQ", "Apple Inc."},
				{"GOOGL", "NASDAQ", "Alphabet Inc."},
				{"TSLA", "NASDAQ", "Tesla Inc."},
				{"AMZN", "NASDAQ", "Amazon Inc."},
				{"MSFT", "NASDAQ", "Microsoft Corp."}
		};
		loadTickers.setOnAction(e -> populateGrid(tickers));
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
