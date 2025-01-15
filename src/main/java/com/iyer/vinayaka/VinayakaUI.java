package com.iyer.vinayaka;

import com.iyer.vinayaka.events.StageReadyEvent;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class VinayakaUI extends Application {
	private static ConfigurableApplicationContext context;
	private static VinayakaUI instance;
	
	@Override
	public void init() {
		context = new SpringApplicationBuilder(VinayakaApplication.class).run();
	}
	
	@Override
	public void start(Stage stage) {
		instance = this;
		context.publishEvent(new StageReadyEvent(stage));
	}
	
	@Override
	public void stop() {
		context.close();
		Platform.exit();
	}
	
	public static HostServices getService() {
		return instance.getHostServices();
	}
}
