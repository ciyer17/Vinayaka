package com.iyer.vinayaka.events;

import com.iyer.vinayaka.util.DataHolder;
import com.iyer.vinayaka.util.UIUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageReadyEventListener implements ApplicationListener<StageReadyEvent> {
	private final ApplicationContext context;
	private final DataHolder dataHolder;
	
	@Autowired
	public StageReadyEventListener(ApplicationContext context, DataHolder holder) {
		this.context = context;
		this.dataHolder = holder;
	}
	
	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		Stage stage = event.getStage();
		dataHolder.setContext(this.context);
		dataHolder.setStage(stage);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(UIUtils.MAIN_VIEW));
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
