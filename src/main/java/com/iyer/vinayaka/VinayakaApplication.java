package com.iyer.vinayaka;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.iyer.vinayaka.repository")
public class VinayakaApplication {

	public static void main(String[] args) {
		Application.launch(VinayakaUI.class, args);
	}
}
