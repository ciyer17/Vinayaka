package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.trader.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlpacaHistoricalBarsDataService {
	private final AlpacaAPI alpacaAPI;
	
	private final String currency = "USD";
	private final long historicalDataLimit = 10000;
	private final String tz = "America/New_York";
	private final ZoneId zoneId = ZoneId.of(tz);
	private final ZoneOffset zoneOffset = ZonedDateTime.now(zoneId).getOffset();
	
	@Autowired
	public AlpacaHistoricalBarsDataService(AlpacaAPI api) {
		this.alpacaAPI = api;
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
		LocalDate validLastTradingDate = LocalDate.now().minusDays(7);
		LocalDate today = LocalDate.now();
		LocalTime fromTime = LocalTime.of(0, 0, 0);
		LocalTime toTime = LocalTime.of(23, 59, 59);
		OffsetDateTime offsetStartTime = OffsetDateTime.of(validLastTradingDate, fromTime, ZoneOffset.UTC);
		OffsetDateTime offsetEndTime = OffsetDateTime.of(today, toTime, ZoneOffset.UTC);
		
		List<StockBar> historicalBars = new ArrayList<>();
		try {
			String timeFrame = "5Min";
			
			validLastTradingDate = this.getStartDay(offsetStartTime, offsetEndTime);
			
			// Update the time frame to match that of New York.
			offsetStartTime = OffsetDateTime.of(validLastTradingDate, fromTime, zoneOffset);
			offsetEndTime = OffsetDateTime.of(today, toTime, zoneOffset);
			
			// If the last trading date is today, get the intraday bars
			if (validLastTradingDate.toString().equals(today.toString())) {
				// 15-minute delay is required due to API restrictions on the free plan
				historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(ticker, timeFrame,
						offsetStartTime, OffsetDateTime.now(zoneOffset).minusMinutes(15), historicalDataLimit,
						StockAdjustment.ALL, null, StockFeed.SIP, currency, null,
						Sort.ASC).getBars();
			} else { // Otherwise, get the last trading day's bars
				historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(ticker, timeFrame,
						offsetStartTime, offsetEndTime, historicalDataLimit, StockAdjustment.ALL, null,
						StockFeed.SIP, currency, null, Sort.ASC).getBars();
			}
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		return historicalBars;
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
		LocalTime fromTime = LocalTime.of(0, 0, 0);
		LocalTime toTime = LocalTime.of(23, 59, 59);
		OffsetDateTime offsetStartTime = OffsetDateTime.of(startDay, fromTime, ZoneOffset.UTC);
		OffsetDateTime offsetEndTime = OffsetDateTime.of(today, toTime, ZoneOffset.UTC);
		
		List<StockBar> historicalBars = new ArrayList<>();
		try {
			startDay = this.getStartDay(offsetStartTime, offsetEndTime);
			
			// Update the time frame to match that of New York.
			offsetStartTime = OffsetDateTime.of(startDay, fromTime, zoneOffset);
			offsetEndTime = OffsetDateTime.of(today, toTime, zoneOffset);
			
			historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(ticker, timeFrame,
					offsetStartTime, offsetEndTime, historicalDataLimit, StockAdjustment.ALL, null,
					StockFeed.SIP, currency, null, Sort.ASC).getBars();
		} catch (net.jacobpeterson.alpaca.openapi.marketdata.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}
		
		return historicalBars;
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
		
		return LocalDate.of(Integer.parseUnsignedInt(splitDate[0]),
				Integer.parseUnsignedInt(splitDate[1]), Integer.parseUnsignedInt(splitDate[2]));
	}
}
