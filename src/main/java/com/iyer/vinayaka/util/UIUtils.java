package com.iyer.vinayaka.util;

import com.iyer.vinayaka.service.AlpacaMarketDataService;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UIUtils {
	/**
	 * Navigates to the specified page.
	 *
	 * @param event The event (e.g. button click) that triggers the navigation.
	 * @param viewToNavigateTo The view to navigate to.
	 * @param prevPageClass Instance of the calling class.
	 */
	public static void navigateToSpecifiedPage(ActionEvent event, String viewToNavigateTo, Class<?> prevPageClass) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		FXMLLoader loader = new FXMLLoader(prevPageClass.getResource(viewToNavigateTo));
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
	 * @param title The title of the alert window.
	 * @param alertMessage The message to display in the alert window.
	 * @param alertType The type of alert to display.
	 */
	public static void showAlert(String title, String alertMessage, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(alertMessage);
		alert.showAndWait();
	}
	
	/**
	 * Validates the API Key and API Secret. The API Key and API Secret are valid if they are not empty, AND if they
	 * are alphanumeric, AND if they are valid Alpaca API keys.
	 *
	 * @param apiKey The API Key to validate.
	 * @param apiSecret The API Secret to validate.
	 * @return True if the API Key and API Secret are valid, false otherwise.
	 */
	public static boolean validateAPIDetails(String apiKey, String apiSecret) {
		boolean valid = false;
		
		if (apiKey.isEmpty() || apiSecret.isEmpty()) {
			showAlert("Empty Fields", "API Key and API Secret cannot be empty.",
					Alert.AlertType.ERROR);
			
		} else if (!checkAPIDetailsLength(apiKey, apiSecret)) {
			showAlert("Invalid Length", "API Key must be 20 characters long and API Secret must be 40 characters long.",
					Alert.AlertType.ERROR);
		} else if (!doAPIDetailsHaveValidCharacters(apiKey, apiSecret)) {
			showAlert("Invalid Characters", "API Key and API Secret can only contain alphanumeric characters.",
					Alert.AlertType.ERROR);
		} else if (!areAPIKeysFromAlpaca(apiKey, apiSecret)) {
			showAlert("Invalid API Details", "API Key and API Secret are not valid Alpaca API keys. Did you make sure " +
							"that these API Details are NOT from the your Alpaca Paper Account?",
					Alert.AlertType.ERROR);
		} else {
			UIUtils.showAlert("API Secrets Saved", "API Key and API Secret saved successfully.",
					Alert.AlertType.INFORMATION);
			valid = true;
		}
		
		return valid;
	}
	
	/**
	 * Checks if the API Key and API Secret are valid. They are valid if an only if
	 * they contain only alphanumeric characters, AND if the API Key is exactly 20 characters
	 * long AND if the API Secret is exactly 40 characters long.
	 *
	 * @param apiKey The API Key to check.
	 * @param apiSecret The API Secret to check.
	 * @return True if the API Key and API Secret are valid, false otherwise.
	 */
	private static boolean doAPIDetailsHaveValidCharacters(String apiKey, String apiSecret) {
		if (apiKey.length() != 20 || apiSecret.length() != 40) {
			return false;
		}
		Pattern specialCharacters = Pattern.compile("[^a-zA-Z0-9 ]");
		Matcher apiKeyMatcher = specialCharacters.matcher(apiKey);
		Matcher apiSecretMatcher = specialCharacters.matcher(apiSecret);
		
		return (!apiKeyMatcher.find() && !apiSecretMatcher.find());
	}
	
	/**
	 * Checks if the API Key and API Secret lengths are valid. API Key must be 20 characters long and
	 * the API Secret must be 40 characters long.
	 *
	 * @param apiKey The API Key to check.
	 * @param apiSecret The API Secret to check.
	 * @return True if the API Key and API Secret lengths are valid, false otherwise.
	 */
	private static boolean checkAPIDetailsLength(String apiKey, String apiSecret) {
		return apiKey.length() == 20 && apiSecret.length() == 40;
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