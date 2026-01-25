package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockQuote;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockTrade;
import net.jacobpeterson.alpaca.openapi.trader.model.Account;
import net.jacobpeterson.alpaca.openapi.trader.model.AssetClass;
import net.jacobpeterson.alpaca.openapi.trader.model.Assets;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlpacaMarketDataService {
	private final AlpacaAPI alpacaAPI;
	private final AlpacaHistoricalBarsDataService historicalBarsDataService;

	private final String currency = "USD";
	private final StockFeed feed = StockFeed.IEX;

	public AlpacaMarketDataService(AlpacaAPI api, AlpacaHistoricalBarsDataService dataService) {
		this.alpacaAPI = api;
		this.historicalBarsDataService = dataService;
	}

	/**
	 * Checks if the given API key and API secret are valid.
	 *
	 * @return True if the API Key and API Secret are valid, false otherwise.
	 */
	public static boolean checkAPIDetails(AlpacaAPI api) {
		Account account;
		try {
			account = api.trader().accounts().getAccount();
		} catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
			account = null;
		}

		return account != null;
	}

	/**
	 * Gets all the valid assets tracked by the Alpaca Markets API.
	 *
	 * @return A list of all the assets tracked by the Alpaca Markets API.
	 */
	public List<Assets> getAllAssets() {
		List<Assets> assets = new ArrayList<>();
		try {
			assets = this.alpacaAPI.trader().assets().getV2Assets("active", AssetClass.US_EQUITY.getValue(),
					null, null);
		} catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}

		return assets;
	}

	/**
	 * Gets the latest stock quotes for the given tickers.
	 *
	 * @param tickers A list of stock tickers. All tickers must
	 *                   be listed on AMEX, ARCA, BATS, NYSE,
	 *                   NASDAQ, or OTC exchanges.
	 *                Not all tickers on the OTC exchange are supported.
	 *
	 * @return A hashmap of stock tickers to their latest quotes. If any
	 * of the tickers are invalid, that ticker's data will not be included.
	 * If all the tickers passed are invalid, an empty HashMap is returned.
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
	 *               The ticker must be listed on NASDAQ, NYSE, or
	 *               the IEX exchange.
	 *
	 * @return A StockQuote object representing the latest quote for
	 * the ticker. If the ticker is not found, the exception message
	 * is printed to the terminal and null is returned.
	 */
	public StockQuote getLatestSingleStockQuote(String ticker) {
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
	 *                be listed on AMEX, ARCA, BATS, NYSE,
	 *                NASDAQ, or OTC exchanges.
	 *                Not all tickers on the OTC exchange are supported.
	 *
	 * @return A hashmap of stock tickers to their latest trades. If any
	 * of the tickers are invalid, that ticker's data will not be included.
	 * If all the tickers passed are invalid, an empty HashMap is returned.
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
	 *               The ticker must be listed on AMEX, ARCA, BATS,
	 *               NYSE, NASDAQ, or OTC exchanges.
	 *               Not all tickers on the OTC exchange are supported.
	 *
	 * @return A StockTrade object representing the latest trade for
	 * the ticker. If the ticker is not found, the exception message
	 * is printed to the terminal and null is returned.
	 */
	public StockTrade getLatestSingleStockTrade(String ticker) {
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
	 *
	 * @return A hashmap containing the official name and listed exchange.
	 * Fetch them using the keys "officialName" and "listedExchange". If the
	 * ticker is not found, the exception message is printed to the terminal
	 * and an empty HashMap is returned.
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

	/**
	 * Gets the latest stock bar for the given ticker.
	 *
	 * @param ticker The ticker whose latest bar is to be fetched.
	 *               The ticker must be listed on AMEX, ARCA, BATS,
	 *               NYSE, NASDAQ, or OTC exchanges.
	 *               Not all tickers on the OTC exchange are supported.
	 *
	 * @return A StockBar object representing the latest bar for
	 * the ticker. If the ticker is not found, the exception message
	 * is printed to the terminal and null is returned.
	 */
	public StockBar getSingleStockBar(String ticker) {
		StockBar bar = null;
		try {
			bar = alpacaAPI.marketData().stock().stockLatestBarSingle(
					ticker, feed, currency).getBar();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}

		return bar;
	}

	/**
	 * Retrieves the latest stock bars and price change percentages for the given
	 * tickers.
	 *
	 * <p>
	 * This is a convenience wrapper method that delegates to
	 * {@link AlpacaHistoricalBarsDataService}
	 * to calculate price changes relative to the previous trading day. It's the
	 * primary method called
	 * by {@link MainViewController} during ticker refresh cycles.
	 * </p>
	 *
	 * <p>
	 * <b>Return Structure:</b> Returns a list containing exactly 2 maps:
	 * </p>
	 * <ol>
	 * <li>Index 0: {@code Map<String, List<StockBar>>} - Latest 1-minute bars for
	 * each ticker</li>
	 * <li>Index 1: {@code Map<String, Double>} - Price change percentages (rounded
	 * to 2 decimals)</li>
	 * </ol>
	 *
	 * <p>
	 * <b>Usage Example:</b>
	 * </p>
	 * 
	 * <pre>{@code
	 * List<Map<String, ?>> result = service.getPriceChangePercentages(List.of("AAPL", "MSFT"));
	 * Map<String, List<StockBar>> latestBars = (Map<String, List<StockBar>>) result.get(0);
	 * Map<String, Double> priceChanges = (Map<String, Double>) result.get(1);
	 * }</pre>
	 *
	 * @param tickersToGetDataFor List of ticker symbols to fetch data for (e.g.,
	 *                            ["AAPL", "MSFT", "GOOGL"])
	 *
	 * @return A list with 2 elements: [0] = latest bars map, [1] = price change
	 *         percentage map.
	 *         Returns empty list if an error occurs or if market data cannot be
	 *         fetched.
	 */
	public List<Map<String, ?>> getPriceChangePercentages(List<String> tickersToGetDataFor) {
		return this.historicalBarsDataService.getLatestPriceChangePercentages(tickersToGetDataFor);
	}

	/**
	 * Gets the historical 1-day stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 1-day bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-day bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1DHistoricalBars(String ticker) {
		return historicalBarsDataService.get1DHistoricalStockBars(ticker);
	}

	/**
	 * Gets the historical 1-Week stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 1-Week bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-Week bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1WHistoricalBars(String ticker) {
		return historicalBarsDataService.get1WHistoricalBars(ticker);
	}

	/**
	 * Gets the historical 1-month stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 1-month bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-month bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1MHistoricalBars(String ticker) {
		return historicalBarsDataService.get1MHistoricalBars(ticker);
	}

	/**
	 * Gets the historical 3-month stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 3-month bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 3-month bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get3MHistoricalBars(String ticker) {
		return historicalBarsDataService.get3MHistoricalBars(ticker);
	}

	/**
	 * Gets the historical 1-year stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 1-year bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-year bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1YHistoricalBars(String ticker) {
		return historicalBarsDataService.get1YHistoricalBars(ticker);
	}

	/**
	 * Gets the historical 5-year stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 5-year bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 5-year bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get5YHistoricalBars(String ticker) {
		return historicalBarsDataService.get5YHistoricalBars(ticker);
	}
}
