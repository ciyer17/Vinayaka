package com.iyer.vinayaka.service;

import com.iyer.vinayaka.entities.UserSettings;
import com.iyer.vinayaka.repository.UserSettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserSettingsService {
	private final UserSettingsRepository userSettingsRepository;
	
	@Autowired
	public UserSettingsService(UserSettingsRepository repository) {
		this.userSettingsRepository = repository;
	}
	
	/**
	 * User settings. Only one user settings object is allowed,
	 * as current assumption is only one user. Profiles later?
	 *
	 * @param settings The made settings object. Contains all preferences.
	 * @return The saved settings object. Since only one object is allowed,
	 * 	   returns null if there is already a settings object, or on
	 * 	   database errors.
	 */
	@Transactional
	public UserSettings addUserSettings(UserSettings settings) {
		if (this.getUserSettings().isEmpty()) {
			return this.userSettingsRepository.save(settings);
		} else {
			return null;
		}
	}
	
	/**
	 * Delete the user settings object.
	 */
	@Transactional
	public void deleteUserSettings() {
		this.userSettingsRepository.deleteById(0);
	}
	
	/**
	 * Get the user preference object.
	 *
	 * @return The user preference object. Empty if not found.
	 */
	public Optional<UserSettings> getUserSettings() {
		List<UserSettings> settings = this.userSettingsRepository.findAll();
		 return settings.stream().findFirst();
	}
	
	/**
	 * Retrieves the API Key and Secret pair in Base64 encoded format.
	 *
	 * @return A List of Strings containing the API Key and Secret,
	 * in that order. Both encoded in Base64 format.
	 */
	public List<String> getAPISecrets() {
		Optional<UserSettings> settings = this.getUserSettings();
		return Arrays.asList(this.getAPIKey(settings), this.getAPISecret(settings));
	}
	
	/**
	 * Retrieve the API Key.
	 *
	 * @return The user supplied API Key. Empty string if not found.
	 */
	private String getAPIKey(Optional<UserSettings> settings) {
		return settings.map(UserSettings::getAPI_KEY).orElse("");
	}
	
	/**
	 * Retrieve the API Secret.
	 *
	 * @return The user supplied API Secret. Empty string if not found.
	 */
	private String getAPISecret(Optional<UserSettings> settings) {
		return settings.map(UserSettings::getAPI_SECRET).orElse("");
	}
	
	/**
	 * Update the Alpaca API Key and Secret pair.
	 *
	 * @param apiKey The Alpaca API Key.
	 * @param apiSecret The Alpaca API Secret.
	 * @return The updated user settings object. Null if not found.
	 */
	@Transactional
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
	@Transactional
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
	@Transactional
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
	@Transactional
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
