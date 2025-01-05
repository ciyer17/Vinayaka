package com.iyer.vinayaka.service;

import lombok.RequiredArgsConstructor;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AlpacaMarketDataService {
	private final AlpacaAPI alpacaAPI;
	private final String currency = "USD";
	private final StockFeed feed = StockFeed.IEX;
	
	/**
	 * Gets the latest stock quotes for the given tickers.
	 *
	 * @param tickers A list of stock tickers. All tickers must
	 * 	 *                   be listed on AMEX, ARCA, BATS, NYSE,
	 * 	 *                   NASDAQ, or OTC exchanges.
	 * 	 *                Not all tickers on the OTC exchange are supported.
	 *
	 * @return A hashmap of stock tickers to their latest quotes.
	 */
	public Map<String, StockQuote> getLatestStockQuotes(List<String> tickers) {
		Map<String, StockQuote> quotes = new HashMap<>();
		try {
			quotes = alpacaAPI.marketData().stock().stockLatestQuotes(
					tickers.parallelStream().collect(Collectors.joining(",")),
					feed, currency).getQuotes();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return quotes;
	}
	
	/**
	 * Gets the latest stock quote for the given ticker.
	 *
	 * @param ticker The ticker whose latest quote is to be fetched.
	 *                  The ticker must be listed on NASDAQ, NYSE, or
	 *                  the IEX exchange.
	 *
	 * @return A StockQuote object representing the latest quote for
	 * the ticker.
	 */
	public StockQuote getSingleStockQuote(String ticker) {
		StockQuote quote = null;
		try {
			quote = this.alpacaAPI.marketData().stock().stockLatestQuoteSingle(
					ticker, feed, currency).getQuote();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return quote;
	}
	
	/**
	 * Gets the latest stock trades for the given tickers.
	 *
	 * @param tickers A list of stock tickers. All tickers must
	 *                   be listed on AMEX, ARCA, BATS, NYSE,
	 *                   NASDAQ, or OTC exchanges.
	 *                Not all tickers on the OTC exchange are supported.
	 *
	 * @return A hashmap of stock tickers to their latest trades.
	 */
	public Map<String, StockTrade> getLatestStockTrades(List<String> tickers) {
		Map<String, StockTrade> trades = new HashMap<>();
		try {
			trades = this.alpacaAPI.marketData().stock().stockLatestTrades(
					tickers.parallelStream().collect(Collectors.joining(",")),
							feed, currency).getTrades();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return trades;
	}
	
	/**
	 * Gets the latest stock trade for the given ticker.
	 *
	 * @param ticker The ticker whose latest trade is to be fetched.
	 *                  The ticker must be listed on AMEX, ARCA, BATS,
	 *                  NYSE, NASDAQ, or OTC exchanges.
	 *               Not all tickers on the OTC exchange are supported.
	 *
	 * @return A StockTrade object representing the latest trade for
	 * the ticker.
	 */
	public StockTrade getSingleStockTrade(String ticker) {
		StockTrade trade = null;
		try {
			trade = this.alpacaAPI.marketData().stock().stockLatestTradeSingle(
					ticker, feed, currency).getTrade();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return trade;
	}
	
	/**
	 * Gets the name and exchange of the given ticker.
	 *
	 * @param ticker The ticker whose name and exchange are to be fetched.
	 * @return A hashmap containing the official name and listed exchange.
	 * Fetch them using the keys "officialName" and "listedExchange".
	 */
	public Map<String, String> getTickerNameAndExchange(String ticker) {
		Map<String, String> tickerNameAndExchange = new HashMap<>();
		try {
			Assets assets = this.alpacaAPI.trader().assets().
					getV2AssetsSymbolOrAssetId(ticker);
			
			tickerNameAndExchange.put("officialName", assets.getName());
			tickerNameAndExchange.put("listedExchange", assets.getExchange().getValue());
		} catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return tickerNameAndExchange;
	}
}
