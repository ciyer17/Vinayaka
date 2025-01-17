package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.VinayakaUI;
import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.service.UserSettingsService;
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
	
	private final UserSettingsService userSettingsService;
	private final DataHolder dataHolder;
	private final UIUtils uiUtils;
	
	public APISecretsController(UserSettingsService userSettingsService, DataHolder dataHolder, UIUtils uiUtils) {
		this.userSettingsService = userSettingsService;
		this.dataHolder = dataHolder;
		this.uiUtils = uiUtils;
	}
	
	public void initialize() {
	
	}
	
	public void saveAPISecrets(ActionEvent event) {
		String apiKey = this.apiKeyField.getText().toUpperCase();
		String apiSecret = this.apiSecretField.getText();
		UserSettings settings = this.dataHolder.getUserSettings();
		
		boolean valid = uiUtils.validateAPIDetails(apiKey, apiSecret);
		if (valid) { // Store the user given API Key and Secret and set others to default.
			settings.setAPI_KEY(apiKey);
			settings.setAPI_SECRET(apiSecret);
			settings.setDark_mode(true);
			settings.setTimezone("America/New_York");
			settings.setRefresh_interval(10);
			this.dataHolder.setUserSettings(settings);
			this.userSettingsService.addUserSettings(settings);
			this.uiUtils.navigateToSpecifiedPage("/view/MainView.fxml", this.getClass());
		}
	}
	
	public void navigateToAlpaca(ActionEvent event) {
		VinayakaUI.getService().showDocument("https://app.alpaca.markets/");
		alpacaHyperlink.setVisited(true);
	}
}
