package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.VinayakaUI;
import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.UIUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

@Controller
public class APISecretsController {
	@FXML
	private TextField apiKeyField;
	
	@FXML
	private TextField apiSecretField;
	
	@FXML
	private Button apiSecretsSaveButton;
	
	@FXML
	private Hyperlink alpacaHyperlink;
	
	public void initialize() {
	
	}
	
	public void saveAPISecrets(ActionEvent event) {
		String apiKey = this.apiKeyField.getText().toUpperCase();
		String apiSecret = this.apiSecretField.getText();
		DataHolder dataHolder = DataHolder.getInstance();
		UserSettings settings = dataHolder.getUserSettings();
		
		boolean valid = UIUtils.validateAPIDetails(apiKey, apiSecret);
		if (valid) {
			settings.setAPI_KEY(apiKey);
			settings.setAPI_SECRET(apiSecret);
		}
	}
	
	public void navigateToAlpaca(ActionEvent event) {
		VinayakaUI.getService().showDocument("https://app.alpaca.markets/");
		alpacaHyperlink.setVisited(true);
	}
}
