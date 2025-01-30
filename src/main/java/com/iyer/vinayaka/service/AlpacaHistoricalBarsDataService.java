package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.*;
import net.jacobpeterson.alpaca.openapi.trader.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class AlpacaHistoricalBarsDataService {
	private final AlpacaAPI alpacaAPI;
	
	private final long historicalDataLimit = 10000;
	private final String currency = "USD";
	private final String tz = "America/New_York";
	private final ZoneId zoneId = ZoneId.of(tz);
	private final ZoneOffset zoneOffset = ZonedDateTime.now(zoneId).getOffset();
	
	@Autowired
	public AlpacaHistoricalBarsDataService(AlpacaAPI api) {
		this.alpacaAPI = api;
	}
	
	/**
	 * Gets the change percentage of the given tickers compared to the last trading day.
	 *
	 * @param tickersToGetDataFor The tickers whose change percentage has to be calculated.
	 * @param latestTrades The latest trade data for all the tickers whose price change is being requested.
	 *
	 * @return A Map of ticker symbols to their respective price change percentages.
	 */
	public Map<String, Double> getLatestPriceChangePercentages(List<String> tickersToGetDataFor,
														 Map<String, List<StockTrade>> latestTrades) {
		
		LocalDate startDay = LocalDate.now().minusDays(7);
		LocalDate today = LocalDate.now();
		LocalTime fromTime = LocalTime.of(19, 59, 0);
		LocalTime toTime = LocalTime.of(20, 0, 0);
		
		OffsetDateTime offsetStartTime = OffsetDateTime.of(startDay, fromTime, ZoneOffset.UTC);
		OffsetDateTime offsetEndTime = OffsetDateTime.of(today, toTime, ZoneOffset.UTC);
		
		Map<String, List<StockBar>> yesterdaysBars;
		Map<String, Double> priceChangePercentages = new HashMap<>();
		try {
			startDay = this.getStartDay(offsetStartTime, offsetEndTime);
			
			// Update the time range to match the New York time zone
			offsetStartTime = OffsetDateTime.of(startDay, fromTime, zoneOffset);
			offsetEndTime = OffsetDateTime.of(today, toTime, zoneOffset);
			
			yesterdaysBars = this.alpacaAPI.marketData().stock().stockBars(
					String.join(",", tickersToGetDataFor), "1D",
					offsetStartTime, offsetEndTime, this.historicalDataLimit, StockAdjustment.ALL,null,
					StockFeed.SIP, this.currency, null, Sort.ASC).getBars();
			
			// The Stream API takes the keySet() (which returns a Set<String> in this case) for all the lambda operations.
			priceChangePercentages = yesterdaysBars.keySet().parallelStream()
					.filter(ticker -> yesterdaysBars.get(ticker) != null && !yesterdaysBars.get(ticker).isEmpty()
							&& latestTrades.get(ticker) != null && !latestTrades.get(ticker).isEmpty())
					.collect(Collectors.toMap(
							ticker -> ticker,
							ticker -> {
								Double yesterdaysClose = yesterdaysBars.get(ticker).getFirst().getC();
								Double currentPrice = latestTrades.get(ticker).getFirst().getP();
								
								return ((currentPrice - yesterdaysClose) / yesterdaysClose) * 100;
							}
					));
					
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return priceChangePercentages;
	}
	
	/**
	 * Gets the historical 1-day stock bars for the given ticker.
	 *
	 * @param ticker The ticker whose 1-day bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-day bars.
	 * If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1DHistoricalStockBars(String ticker) {
		LocalDate startDay = LocalDate.now().minusDays(7);
		LocalDate today = LocalDate.now();
		String timeFrame = "5Min";
		
		return this.fetchHistoricalBars(ticker, startDay, today, timeFrame);
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
		return this.getAppropriateHistoricalBars(ticker,7, "1H");
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
		LocalDate nMonthsAgo = LocalDate.now().minusMonths(1);
		long daysToSubtract = ChronoUnit.DAYS.between(nMonthsAgo, LocalDate.now());
		
		return this.getAppropriateHistoricalBars(ticker, daysToSubtract, "1H");
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
		LocalDate nMonthsAgo = LocalDate.now().minusMonths(3);
		long daysToSubtract = ChronoUnit.DAYS.between(nMonthsAgo, LocalDate.now());
		
		return this.getAppropriateHistoricalBars(ticker, daysToSubtract, "1D");
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
		LocalDate nYearsAgo = LocalDate.now().minusYears(1);
		long daysToSubtract = ChronoUnit.DAYS.between(nYearsAgo, LocalDate.now());
		
		return this.getAppropriateHistoricalBars(ticker, daysToSubtract, "1D");
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
		LocalDate nYearsAgo = LocalDate.now().minusYears(5);
		long daysToSubtract = ChronoUnit.DAYS.between(nYearsAgo, LocalDate.now());
		
		return this.getAppropriateHistoricalBars(ticker, daysToSubtract, "7D");
	}
	
	/**
	 * Fetches the historical bars for the given ticker between the given time range divided by the given timeframe.
	 *
	 * @param ticker The ticker whose historical bars are to be fetched.
	 * @param startDay The first day of the historical data to be fetched.
	 * @param endDay The last day of the historical data to be fetched.
	 * @param timeFrame The timeframe (or interval) of the bars to be aggregated.
	 * @return A list of StockBar objects containing the historical bars for the requested ticker.
	 */
	private List<StockBar> fetchHistoricalBars(String ticker, LocalDate startDay, LocalDate endDay, String timeFrame) {
		LocalTime fromTime = LocalTime.of(0, 0, 0);
		LocalTime toTime = LocalTime.of(23, 59, 59);
		OffsetDateTime offsetStartTime = OffsetDateTime.of(startDay, fromTime, ZoneOffset.UTC);
		OffsetDateTime offsetEndTime = OffsetDateTime.of(endDay, toTime, ZoneOffset.UTC);
		
		List<StockBar> historicalBars = new ArrayList<>();
		boolean isIntradayRequest = this.isIntradayRequest();
		
		try {
			startDay = this.getStartDay(offsetStartTime, offsetEndTime);
			
			// Update the time range to match the New York time zone
			offsetStartTime = OffsetDateTime.of(startDay, fromTime, zoneOffset);
			offsetEndTime = OffsetDateTime.of(endDay, toTime, zoneOffset);
			
			// If the request is for intraday data
			if (isIntradayRequest && startDay.isEqual(LocalDate.now())) {
				// 15-minute delay is required due to API restrictions on the free plan
				historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(
						ticker, timeFrame, offsetStartTime,
						OffsetDateTime.now(zoneOffset).minusMinutes(15),
						historicalDataLimit, StockAdjustment.ALL, null,
						StockFeed.SIP, currency, null, Sort.ASC
				).getBars();
			} else {
				// General case
				historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(
						ticker, timeFrame, offsetStartTime, offsetEndTime,
						historicalDataLimit, StockAdjustment.ALL, null,
						StockFeed.SIP, currency, null, Sort.ASC
				).getBars();
			}
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return historicalBars;
	}
	
	/**
	 * Determines if the request could be an intraday request.
	 *
	 * @return True if the request could be an intraday request, false otherwise.
	 */
	private boolean isIntradayRequest() {
		StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
		return traceElements.length > 2 && traceElements[2].getMethodName().equals("get1DHistoricalStockBars");
	}
	
	/**
	 * Gets the appropriate historical stock bars for the given ticker,
	 * for the given duration, aggregated by the given timeframe.
	 *
	 * @param ticker The ticker whose bars are to be fetched.
	 * @param daysToSubtract The number of days to subtract from the
	 *                       current date.
	 * @param timeFrame The timeframe to aggregate the bars by. Could
	 *                  be any variation of "1Min", "1H", "1D", "1W",
	 *                  "1M", "1Y", where you can replace the "1" with
	 *                  the desired number of minutes, hours, days, weeks,
	 *                  months, or years.
	 *
	 * @return A list of StockBar objects representing the historical bars.
	 * If the ticker is not found, null is returned.
	 */
	private List<StockBar> getAppropriateHistoricalBars(String ticker, long daysToSubtract, String timeFrame) {
		LocalDate startDay = LocalDate.now().minusDays(daysToSubtract);
		LocalDate today = LocalDate.now();
		
		return this.fetchHistoricalBars(ticker, startDay, today, timeFrame);
	}
	
	/**
	 * Calculates the last trading day based on the given start and end times.
	 *
	 * @param offsetStartTime The start time of the historical data.
	 * @param offsetEndTime The end time of the historical data.
	 *
	 * @return A LocalDate object representing the last trading day.
	 */
	private LocalDate getStartDay(OffsetDateTime offsetStartTime,  OffsetDateTime offsetEndTime) {
		Calendar validLastTradingDay = new Calendar();
		try {
			List<Calendar> marketCalendar = this.alpacaAPI.trader().calendar().
					getCalendar(offsetStartTime, offsetEndTime, "TRADING");
			validLastTradingDay = marketCalendar.getFirst();
		} catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		String date = validLastTradingDay.getDate();
		String[] splitDate = date.split("-");
		
		// Year-Month-Day
		return LocalDate.of(Integer.parseUnsignedInt(splitDate[0]),
				Integer.parseUnsignedInt(splitDate[1]), Integer.parseUnsignedInt(splitDate[2]));
	}
}
