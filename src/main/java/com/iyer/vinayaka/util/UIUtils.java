package com.iyer.vinayaka.util;

import com.iyer.vinayaka.service.AlpacaMarketDataService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UIUtils {
	public static final String MAIN_VIEW = "/view/MainView.fxml";
	public static final String UPDATE_SETTINGS_VIEW = "/view/UpdateSettings.fxml";
	public static final String ENTER_API_SECRETS_VIEW = "/view/APISecrets.fxml";
	public static final String TICKER_VIEW = "/view/TickerView.fxml";

	public static final String DARK_MODE_IMG = "/icons/settings-dark-mode.png";
	public static final String LIGHT_MODE_IMG = "/icons/settings-light-mode.png";
	public static final String SEARCH_TICKER = "/icons/search-ticker.png";
	public static final String DELETE_ICON = "/icons/delete.png";

	public static final String DARK_MODE_BG = "appBackground";
	public static final String LIGHT_MODE_BG = "appBackgroundLight";
	public static final String DARK_MODE_LABEL = "infoLabels";
	public static final String LIGHT_MODE_LABEL = "infoLabelsLight";
	public static final String DARK_MODE_TEXTFIELD = "infoTextFields";
	public static final String LIGHT_MODE_TEXTFIELD = "infoTextFieldsLight";

	public static final int API_KEY_LEN = 26;
	public static final int API_SECRET_LEN = 44;

	private final DataHolder dataHolder;

	public UIUtils(DataHolder dataHolder) {
		this.dataHolder = dataHolder;
	}

	/**
	 * Navigates to the specified page.
	 *
	 * @param viewToNavigateTo The view to navigate to.
	 * @param prevPageClass Instance of the calling class.
	 */
	public void navigateToSpecifiedPage(String viewToNavigateTo, Class<?> prevPageClass) {
		Stage stage = this.dataHolder.getStage();
		FXMLLoader loader = new FXMLLoader(prevPageClass.getResource(viewToNavigateTo));
		loader.setControllerFactory(this.dataHolder.getContext()::getBean);
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(prevPageClass.getResource("/css/stylesheet.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Shows the specified alert.
	 * 
	 * @param title        The title of the alert window.
	 * @param alertMessage The message to display in the alert window.
	 * @param alertType    The type of alert to display.
	 * @return The ButtonType clicked by the user, or empty if dialog was closed.
	 */
	public Optional<ButtonType> showAlert(String title, String alertMessage, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(alertMessage);
		return alert.showAndWait();
	}

	/**
	 * Shows a confirmation dialog with Yes/No buttons.
	 *
	 * @param title   The title of the confirmation dialog.
	 * @param message The confirmation message to display.
	 * @return True if the user clicked Yes, false if the user clicked No or closed
	 *         the dialog.
	 */
	public boolean showConfirmationDialog(String title, String message) {
		Optional<ButtonType> result = showAlert(title, message, AlertType.CONFIRMATION);
		return result.isPresent() && (result.get() == ButtonType.YES || result.get() == ButtonType.OK);
	}

	/**
	 * Validates the API Key and API Secret. The API Key and API Secret are valid if they are not empty, AND if they
	 * are alphanumeric, AND if they are valid Alpaca API keys.
	 *
	 * @param apiKey The API Key to validate.
	 * @param apiSecret The API Secret to validate.
	 * @return True if the API Key and API Secret are valid, false otherwise.
	 */
	public boolean validateAPIDetails(String apiKey, String apiSecret) {
		boolean valid = false;

		if (apiKey.isEmpty() || apiSecret.isEmpty()) {
			this.showAlert("Empty Fields", "API Key and API Secret cannot be empty.",
					Alert.AlertType.ERROR);

		} else if (!checkAPIDetailsLength(apiKey, apiSecret)) {
			System.out.println("API Key Length: " + apiKey.length() + "\nAPI Secret Length: " + apiSecret.length());
					this.showAlert("Invalid Length",
					"API Key must be " + API_KEY_LEN + " characters long and API Secret must be " + API_SECRET_LEN
							+ " characters long.",
					Alert.AlertType.ERROR);
		} else if (!doAPIDetailsHaveValidCharacters(apiKey, apiSecret)) {
			this.showAlert("Invalid Characters", "API Key and API Secret can only contain alphanumeric characters.",
					Alert.AlertType.ERROR);
		} else if (!areAPIKeysFromAlpaca(apiKey, apiSecret)) {
			this.showAlert("Invalid API Details", "API Key and API Secret are not valid Alpaca API keys. Did you make sure " +
							"that these API Details are NOT from your Alpaca Paper Account?",
					Alert.AlertType.ERROR);
		} else {
			this.showAlert("API Secrets Saved", "API Key and API Secret saved successfully.",
					Alert.AlertType.INFORMATION);
			valid = true;
		}

		return valid;
	}

	/**
	 * Checks if the API Key and API Secret are valid. They are valid if and only if
	 * they contain only alphanumeric characters, AND if the API Key is exactly
	 * {@API_KEY_LEN} characters
	 * long AND if the API Secret is exactly {@API_SECRET_LEN} characters long.
	 *
	 * @param apiKey    The API Key to check.
	 * @param apiSecret The API Secret to check.
	 * @return True if the API Key and API Secret are valid, false otherwise.
	 */
	private static boolean doAPIDetailsHaveValidCharacters(String apiKey, String apiSecret) {
		Pattern specialCharacters = Pattern.compile("[^a-zA-Z0-9 ]");
		Matcher apiKeyMatcher = specialCharacters.matcher(apiKey);
		Matcher apiSecretMatcher = specialCharacters.matcher(apiSecret);

		return (!apiKeyMatcher.find() && !apiSecretMatcher.find());
	}

	/**
	 * Checks if the API Key and API Secret lengths are valid. API Key must be
	 * {@API_KEY_LEN} characters long and
	 * the API Secret must be {@API_SECRET_LEN} characters long.
	 *
	 * @param apiKey    The API Key to check.
	 * @param apiSecret The API Secret to check.
	 * @return True if the API Key and API Secret lengths are valid, false
	 *         otherwise.
	 */
	private static boolean checkAPIDetailsLength(String apiKey, String apiSecret) {
		return apiKey.length() == API_KEY_LEN && apiSecret.length() == API_SECRET_LEN;
	}

	/**
	 * Checks if the API Key and API Secret are from Alpaca.
	 *
	 * @param apiKey The API Key to check.
	 * @param apiSecret The API Secret to check.
	 * @return True if the API Key and API Secret are from Alpaca, false otherwise.
	 */
	private static boolean areAPIKeysFromAlpaca(String apiKey, String apiSecret) {
		AlpacaAPI api = new AlpacaAPI(apiKey, apiSecret, TraderAPIEndpointType.LIVE,
				MarketDataWebsocketSourceType.IEX);
		return AlpacaMarketDataService.checkAPIDetails(api);
	}
}
