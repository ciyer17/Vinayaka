package com.iyer.vinayaka.util;

import com.iyer.vinayaka.service.UserSettingsService;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
public class AlpacaConfiguration {
	
	private final UserSettingsService userSettingsService;
	
	@Autowired
	public AlpacaConfiguration(@Lazy UserSettingsService service) {
		this.userSettingsService = service;
	}
	
	@Bean
	public AlpacaAPI alpacaAPI() {
		List<String> settings = this.userSettingsService.getAPISecrets();
		
		return new AlpacaAPI(settings.getFirst(), settings.getLast(),
				TraderAPIEndpointType.LIVE,
				MarketDataWebsocketSourceType.IEX);
	}
}
