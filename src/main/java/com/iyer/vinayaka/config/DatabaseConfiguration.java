package com.iyer.vinayaka.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Database configuration for SQLite.
 * Sets the database URL property dynamically based on the platform and ensures
 * the config directory exists before the application context loads.
 */
public class DatabaseConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String APP_NAME = "vinayaka";
	private static final String APP_NAME_WINDOWS = "Vinayaka";
	private static final String DB_FILENAME = "vinayaka.db";

	@Override
	public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
		Path configDir = getConfigDirectory();
		ensureDirectoryExists(configDir);

		Path dbPath = configDir.resolve(DB_FILENAME);
		String jdbcUrl = "jdbc:sqlite:" + dbPath.toString();

		System.setProperty("spring.datasource.url", jdbcUrl);
	}

	/**
	 * Returns the platform-specific configuration directory for the application.
	 * <ul>
	 *   <li>Linux/macOS: ~/.config/vinayaka/</li>
	 *   <li>Windows: %APPDATA%\Vinayaka\</li>
	 * </ul>
	 */
	private static Path getConfigDirectory() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			if (appData != null) {
				return Paths.get(appData, APP_NAME_WINDOWS);
			}
			return Paths.get(System.getProperty("user.home"), "AppData", "Roaming", APP_NAME_WINDOWS);
		} else {
			return Paths.get(System.getProperty("user.home"), ".config", APP_NAME);
		}
	}

	private static void ensureDirectoryExists(Path directory) {
		if (!Files.exists(directory)) {
			try {
				Files.createDirectories(directory);
			} catch (IOException e) {
				throw new RuntimeException("Failed to create config directory: " + directory, e);
			}
		}
	}
}
