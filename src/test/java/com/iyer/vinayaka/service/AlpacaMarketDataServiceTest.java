package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

class AlpacaMarketDataServiceTest {
	@Mock
	private AlpacaHistoricalBarsDataService historicalBarsDataService;
	private AlpacaMarketDataService marketDataService;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		AlpacaAPI api = new AlpacaAPI("",
				"",
				TraderAPIEndpointType.LIVE, MarketDataWebsocketSourceType.IEX);
		marketDataService = new AlpacaMarketDataService(api, historicalBarsDataService);
	}
	
	@Test
	void checkAPIDetailsValid() {
		// Valid API Details
		String apiKey = "";
		String apiSecret = "";
		AlpacaAPI api = new AlpacaAPI(apiKey, apiSecret,
				TraderAPIEndpointType.LIVE, MarketDataWebsocketSourceType.IEX);
		boolean valid = AlpacaMarketDataService.checkAPIDetails(api);
		Assertions.assertTrue(valid);
	}
	
	@Test
	void checkAPIDetailsInvalid() {
		// Invalid API Details
		String apiKey = "ABCDEFGHIJKLMNOPQRST";
		String apiSecret = "ABCDEFGHIJKLMNOPQRSTUVWXyz01234567890123";
		AlpacaAPI api = new AlpacaAPI(apiKey, apiSecret,
				TraderAPIEndpointType.LIVE, MarketDataWebsocketSourceType.IEX);
		boolean valid = AlpacaMarketDataService.checkAPIDetails(api);
		Assertions.assertFalse(valid);
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
	void getLatestSingleStockQuoteValid() {
		String ticker = "PLTR";
		StockQuote quote = this.marketDataService.getLatestSingleStockQuote(ticker);
		
		Assertions.assertNotNull(quote);
	}
	
	@Test
	void getLatestSingleStockQuoteInvalid() {
		String ticker = "PLTRLSDJFOJOS";
		StockQuote quote = this.marketDataService.getLatestSingleStockQuote(ticker);
		
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
	void getLatestSingleStockTrade() {
		String ticker = "PLTR";
		StockTrade trade = this.marketDataService.getLatestSingleStockTrade(ticker);
		
		Assertions.assertNotNull(trade);
	}
	
	@Test
	void getLatestSingleStockTradeInvalid() {
		String ticker = "PLTRSJDFJ";
		StockTrade trade = this.marketDataService.getLatestSingleStockTrade(ticker);
		
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
	void getSingleStockBarValid() {
		String ticker = "PLTR";
		StockBar trade = this.marketDataService.getSingleStockBar(ticker);
		
		Assertions.assertNotNull(trade);
	}
	
	@Test
	void getSingleStockBarInvalid() {
		String ticker = "PLTRSJDFJ";
		StockBar trade = this.marketDataService.getSingleStockBar(ticker);
		
		Assertions.assertNull(trade);
	}
	
	@Test
	void getHistorical1DStockBars() {
	}
}
