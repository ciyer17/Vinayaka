package com.iyer.vinayaka.controller;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.service.UserSettingsService;
import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.ZoneId;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class UpdateSettingsController implements Initializable {
	
	@FXML private TextField preferenceAPIKey;
	@FXML private TextField preferenceAPISecret;
	@FXML private CheckBox darkModePreference;
	@FXML private ChoiceBox<String> localTimeZone;
	@FXML private ChoiceBox<Integer> refreshInterval;
	
	private final UserSettingsService userSettingsService;
	private final UIUtils uiUtils;
	private final DataHolder dataHolder;
	private UserSettings settings;
	
	@Autowired
	public UpdateSettingsController(UserSettingsService service,  UIUtils utils,  DataHolder holder) {
		this.userSettingsService = service;
		this.uiUtils = utils;
		this.dataHolder = holder;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		List<String> tzs = ZoneId.getAvailableZoneIds().parallelStream().sorted().toList();
		this.localTimeZone.setItems(FXCollections.observableList(tzs));
		List<Integer> possibleRefreshIntervals = List.of(5, 10, 15, 30, 60);
		this.refreshInterval.setItems(FXCollections.observableList(possibleRefreshIntervals));
		
		this.settings = this.dataHolder.getUserSettings();
		System.out.println(this.settings);
		
		this.preferenceAPIKey.setText(this.settings.getAPI_KEY());
		this.preferenceAPISecret.setText(this.settings.getAPI_SECRET());
		boolean darkMode = this.settings.getDark_mode();
		this.darkModePreference.setSelected(darkMode);
		this.localTimeZone.setValue(this.settings.getTimezone());
		this.refreshInterval.setValue(this.settings.getRefresh_interval());
	}
	
	public void updateSettings(ActionEvent event) {
		String apiKey = this.preferenceAPIKey.getText();
		String apiSecret = this.preferenceAPISecret.getText();
		if (this.uiUtils.validateAPIDetails(apiKey, apiSecret)) {
			boolean darkModePreference = this.darkModePreference.isSelected();
			String newTz = this.localTimeZone.getValue();
			Integer refreshInterval = this.refreshInterval.getValue();
			
			this.dataHolder.setUserSettings(
					this.userSettingsService.updateSettings(this.settings, apiKey, apiSecret, refreshInterval,
					darkModePreference, newTz)
			);
			System.out.println(this.dataHolder.getUserSettings());
			this.uiUtils.navigateToSpecifiedPage("/view/MainView.fxml", this.getClass());
		}
	}
	
	public void cancelUpdate(ActionEvent event) {
		this.uiUtils.navigateToSpecifiedPage("/view/MainView.fxml", this.getClass());
	}
}
