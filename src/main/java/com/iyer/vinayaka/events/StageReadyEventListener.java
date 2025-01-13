package com.iyer.vinayaka.events;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageReadyEventListener implements ApplicationListener<StageReadyEvent> {
	private final ApplicationContext context;
	
	public StageReadyEventListener(ApplicationContext context) {
		this.context = context;
	}
	
	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		Stage stage = event.getStage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
		loader.setControllerFactory(this.context::getBean);
		try {
			stage.setScene(new Scene(loader.load()));
		} catch (IOException e) {
			System.out.println("Error loading the MainView.");
			throw new RuntimeException(e);
		}
		stage.show();
	}
}
