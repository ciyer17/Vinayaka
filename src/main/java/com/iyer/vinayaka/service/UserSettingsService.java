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
	 * Update the user settings.
	 *
	 * @param apiKey The new Alpaca API key.
	 * @param apiSecret The new Alpaca API Secret.
	 * @param interval The new refresh interval.
	 * @param darkModePreference The new dark mode preference.
	 * @param tz The new local timezone.
	 *
	 * @return The updated UserSettings object. Null if user settings weren't already initialized.
	 */
	public UserSettings updateSettings(UserSettings settings, String apiKey, String apiSecret, Integer interval,
									   Boolean darkModePreference, String tz) {
		
		settings.setAPI_KEY(apiKey);
		settings.setAPI_SECRET(apiSecret);
		settings.setRefresh_interval(interval);
		settings.setDark_mode(darkModePreference);
		settings.setTimezone(tz);
		return this.userSettingsRepository.save(settings);
	}
}
