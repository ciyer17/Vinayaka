package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class AlpacaMarketDataServiceTest {
	private AlpacaMarketDataService marketDataService;
	
	@BeforeEach
	void setUp() {
		AlpacaAPI api = new AlpacaAPI("",
				"",
				TraderAPIEndpointType.LIVE, MarketDataWebsocketSourceType.IEX);
		marketDataService = new AlpacaMarketDataService(api);
	}
	
	@Test
	void getLatestStockQuotesAllValid() {
		List<String> tickers = List.of("AAPL", "NVDA");
		Map<String, StockQuote> quotes = this.marketDataService.getLatestStockQuotes(tickers);
		
		Assertions.assertEquals(2, quotes.size());
	}
	
	@Test
	void getLatestStockQuotesSomeValid() {
		List<String> tickers = List.of("AAPLSJDL", "NVDA");
		Map<String, StockQuote> quotes = this.marketDataService.getLatestStockQuotes(tickers);
		
		Assertions.assertEquals(1, quotes.size());
	}
	
	@Test
	void getLatestStockQuotesAllInvalid() {
		List<String> tickers = List.of("AAPLJKDLFF", "NVDASJDL");
		Map<String, StockQuote> quotes = this.marketDataService.getLatestStockQuotes(tickers);
		
		Assertions.assertEquals(0, quotes.size());
	}
	
	@Test
	void getSingleStockQuoteValid() {
		String ticker = "PLTR";
		StockQuote quote = this.marketDataService.getSingleStockQuote(ticker);
		
		Assertions.assertNotNull(quote);
	}
	
	@Test
	void getSingleStockQuoteInvalid() {
		String ticker = "PLTRLSDJFOJOS";
		StockQuote quote = this.marketDataService.getSingleStockQuote(ticker);
		
		Assertions.assertNull(quote);
	}
	
	@Test
	void getLatestStockTradesAllValid() {
		List<String> tickers = List.of("AAPL", "NVDA");
		Map<String, StockTrade> trades = this.marketDataService.getLatestStockTrades(tickers);
		
		Assertions.assertEquals(2, trades.size());
	}
	
	@Test
	void getLatestStockTradesSomeValid() {
		List<String> tickers = List.of("AAPL", "NVDASDJFLSOIF");
		Map<String, StockTrade> trades = this.marketDataService.getLatestStockTrades(tickers);
		
		Assertions.assertEquals(1, trades.size());
	}
	
	@Test
	void getLatestStockTradesAllInvalid() {
		List<String> tickers = List.of("AAPLSJDFJ", "NVDASDFIJSD");
		Map<String, StockTrade> trades = this.marketDataService.getLatestStockTrades(tickers);
		
		Assertions.assertEquals(0, trades.size());
	}
	
	@Test
	void getSingleStockTrade() {
		String ticker = "PLTR";
		StockTrade trade = this.marketDataService.getSingleStockTrade(ticker);
		
		Assertions.assertNotNull(trade);
	}
	
	@Test
	void getSingleStockTradeInvalid() {
		String ticker = "PLTRSJDFJ";
		StockTrade trade = this.marketDataService.getSingleStockTrade(ticker);
		
		Assertions.assertNull(trade);
	}
	
	@Test
	void getTickerNameAndExchangeValid() {
		String ticker = "NVDA";
		Map<String, String> nameAndExchange = this.marketDataService.getTickerNameAndExchange(ticker);
		
		Assertions.assertEquals(2, nameAndExchange.size());
		Assertions.assertEquals("NASDAQ", nameAndExchange.get("listedExchange"));
		Assertions.assertEquals("NVIDIA Corporation Common Stock", nameAndExchange.get("officialName"));
	}
	
	@Test
	void getTickerNameAndExchangeInvalid() {
		String ticker = "NVDADJFFS";
		Map<String, String> nameAndExchange = this.marketDataService.getTickerNameAndExchange(ticker);
		
		Assertions.assertEquals(0, nameAndExchange.size());
	}
	
	@Test
	void getHistorical1DStockBars() {
	}
}
