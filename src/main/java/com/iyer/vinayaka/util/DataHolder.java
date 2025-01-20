package com.iyer.vinayaka.util;

import com.iyer.vinayaka.entities.UserSettings;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("singleton")
@Setter
@Getter
@Component
public class DataHolder {
	@NonNull
	private UserSettings userSettings;
	private ApplicationContext context;
	private Stage stage;
	
	public DataHolder() {
		this.userSettings = new UserSettings();
	}
}
