package com.iyer.vinayaka.util;

import com.iyer.vinayaka.entities.UserSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;

@Scope("singleton")
@Setter

public class DataHolder {
	@Getter
	private final static DataHolder instance = new DataHolder();
	
	@Getter
	private UserSettings userSettings;
	
	private DataHolder() {}
}
