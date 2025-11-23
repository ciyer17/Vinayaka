package com.iyer.vinayaka;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import net.jacobpeterson.alpaca.openapi.trader.ApiException;
import net.jacobpeterson.alpaca.openapi.trader.model.AssetClass;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;

import java.util.List;

class Main {
	public static void main(String[] args) {
		AlpacaAPI api = new AlpacaAPI("AKVEKID0P04QY8PLKC1O", "103iBNglmFWVBIYXZ292l1vWwO6dw6zZIKq7xcUE", TraderAPIEndpointType.LIVE, MarketDataWebsocketSourceType.SIP);
		
		/*final String tz = "America/New_York";
		final ZoneId zoneId = ZoneId.of(tz);
		final ZoneOffset zoneOffset = ZonedDateTime.now(zoneId).getOffset();
		
		LocalDate startDay = LocalDate.now().minusDays(7);
		LocalDate endDay = LocalDate.now();
		LocalTime endTime = LocalTime.now().minusMinutes(15);
		
		LocalTime fromTime = LocalTime.of(15, 59, 0);
		LocalTime toTime = LocalTime.of(16, 0, 0);
		
		OffsetDateTime offsetStartTime = OffsetDateTime.of(startDay, fromTime, zoneOffset);
		OffsetDateTime offsetEndTime = OffsetDateTime.of(endDay, toTime, zoneOffset);
		
		String[] tickers = {"AAPL", "TSLA", "MSFT", "GOOGL", "AMZN"};
		// System.out.println(api.marketData().stock().stockBars();*/
		
		try {
			List<Assets> assets = api.trader().assets().getV2Assets("active", AssetClass.US_EQUITY.getValue(), null, null);
			System.out.println(assets.getLast().getSymbol());
			System.out.println(assets.size());
		} catch (ApiException e) {
			throw new RuntimeException(e);
		}
	}
}
