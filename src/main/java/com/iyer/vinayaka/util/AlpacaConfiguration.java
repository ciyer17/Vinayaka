package com.iyer.vinayaka.util;

import com.iyer.vinayaka.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AlpacaConfiguration {
	
	private final UserSettingsService userSettingsService;
	
	@Bean
	public AlpacaAPI alpacaAPI() {
		List<String> settings = this.userSettingsService.getAPISecrets();
		
		return new AlpacaAPI(settings.getFirst(), settings.getLast(),
				TraderAPIEndpointType.LIVE,
				MarketDataWebsocketSourceType.IEX);
	}
}
