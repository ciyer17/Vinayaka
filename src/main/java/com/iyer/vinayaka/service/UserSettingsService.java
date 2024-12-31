package com.iyer.vinayaka.service;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.repository.UserSettingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSettingsService {
	private final UserSettingsRepository userSettingsRepository;
	
	/**
	 * User settings. Only one user settings object is allowed,
	 * as current assumption is only one user. Profiles later?
	 *
	 * @param settings The made settings object. Contains all preferences.
	 * @return The saved settings object. Since only one object is allowed,
	 * 	   returns null if there is already a settings object, or on
	 * 	   database errors.
	 */
	public UserSettings addUserSettings(UserSettings settings) {
		if (this.getUserSettings().isEmpty()) {
			return this.userSettingsRepository.save(settings);
		} else {
			return null;
		}
	}
	
	/**
	 /* Get the user preference object.
	 
	 * @return The user preference object. Empty if not found.
	 */
	public Optional<UserSettings> getUserSettings() {
		return this.userSettingsRepository.findById(0);
	}
	
	/**
	 * Update the Alpaca API Key and Secret pair.
	 *
	 * @param apiKey The Alpcaa API Key.
	 * @param apiSecret The Alpaca API Secret.
	 * @return The updated user settings object. Null if not found.
	 */
	public UserSettings updateAPISettings(String apiKey, String apiSecret) {
		Optional<UserSettings> settings = this.getUserSettings();
		if (settings.isPresent()) {
			UserSettings userSettings = settings.get();
			userSettings.setAPI_KEY(apiKey);
			userSettings.setAPI_SECRET(apiSecret);
			return this.userSettingsRepository.save(userSettings);
		}
		
		return null;
	}
	
	/**
	 * Update the refresh interval.
	 *
	 * @param interval The new refresh interval, in seconds.
	 * @return The updated user settings object. Null if not found.
	 */
	public UserSettings updateRefreshInterval(Integer interval) {
		Optional<UserSettings> settings = this.getUserSettings();
		if (settings.isPresent()) {
			UserSettings userSettings = settings.get();
			userSettings.setRefresh_interval(interval);
			return this.userSettingsRepository.save(userSettings);
		}
		
		return null;
	}
	
	/**
	 * Update the dark mode preference.
	 *
	 * @param darkMode True for dark mode, and false for light mode.
	 * @return The updated user settings object. Null if not found.
	 */
	public UserSettings updateDarkMode(Boolean darkMode) {
		Optional<UserSettings> settings = this.getUserSettings();
		if (settings.isPresent()) {
			UserSettings userSettings = settings.get();
			userSettings.setDark_mode(darkMode);
			return this.userSettingsRepository.save(userSettings);
		}
		
		return null;
	}
	
	/**
	 * Update the timezone.
	 *
	 * @param timezone The new timezone. Must be in IANA timezone format.
	 * @return The updated user settings object. Null if not found.
	 */
	public UserSettings updateTimezone(String timezone) {
		Optional<UserSettings> settings = this.getUserSettings();
		if (settings.isPresent()) {
			UserSettings userSettings = settings.get();
			userSettings.setTimezone(timezone);
			return this.userSettingsRepository.save(userSettings);
		}
		
		return null;
	}
}
