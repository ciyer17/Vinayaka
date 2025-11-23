package com.iyer.vinayaka.service;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar;
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed;
import net.jacobpeterson.alpaca.openapi.trader.model.Calendar;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

	// Market calendar cache - invalidates daily at midnight NYC time
	private List<Calendar> cachedMarketCalendar;
	private LocalDate marketCalendarCacheDate;

	public AlpacaHistoricalBarsDataService(AlpacaAPI api) {
		this.alpacaAPI = api;
	}

	/**
	 * Bundles NYC timezone-related time values to ensure consistency within a
	 * single method execution.
	 * All values are derived from the same instant to guarantee they are coherent.
	 */
	private record NYCTimeInfo(ZonedDateTime now, LocalDate today, LocalTime time, ZoneOffset offset) {
	}

	/**
	 * Creates a fresh NYCTimeInfo with current NYC time values.
	 * Call this at the start of any method that needs NYC time information.
	 *
	 * @return A NYCTimeInfo record containing the current NYC time values.
	 */
	private NYCTimeInfo getNYCTimeInfo() {
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return new NYCTimeInfo(now, now.toLocalDate(), now.toLocalTime(), now.getOffset());
	}

	/**
	 * Gets the market calendar for the last 7 days, using a daily cache.
	 * The cache invalidates at midnight NYC time (when the date changes).
	 *
	 * @return A list of Calendar objects representing trading days, or null if an error occurs.
	 */
	private List<Calendar> getMarketCalendar() {
		NYCTimeInfo nycTime = getNYCTimeInfo();

		// Return cached calendar if it's from today
		if (cachedMarketCalendar != null && nycTime.today().equals(marketCalendarCacheDate)) {
			return cachedMarketCalendar;
		}

		// Cache is stale or empty - fetch fresh data
		LocalDate startDay = nycTime.today().minusDays(7);
		OffsetDateTime rangeStart = OffsetDateTime.of(startDay, LocalTime.MIN, nycTime.offset());
		OffsetDateTime rangeEnd = OffsetDateTime.of(nycTime.today(), LocalTime.MAX, nycTime.offset());

		try {
			cachedMarketCalendar = this.alpacaAPI.trader().calendar().
					getCalendar(rangeStart, rangeEnd, "TRADING");
			marketCalendarCacheDate = nycTime.today();
			return cachedMarketCalendar;
		} catch (net.jacobpeterson.alpaca.openapi.trader.ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
			return null;
		}
	}

	/**
	 * Gets the change percentage of the given tickers compared to the last trading day.
	 *
	 * @param tickersToGetDataFor The tickers whose change percentage has to be calculated.
	 *
	 * @return An array list of map of the latest bars and the price change percentages. The first map
	 * is a {@code Map<String, List<StockBar>>} which is the latest bars, and the second map is a
	 * {@code Map<String, Double>} which is the price change percentages. Cast it accordingly.
	 * If there's an error, an empty (array)list is returned.
	 */
	public List<Map<String, ?>> getLatestPriceChangePercentages(List<String> tickersToGetDataFor) {
		NYCTimeInfo nycTime = this.getNYCTimeInfo();

		LocalTime fromTime = LocalTime.of(15, 59, 0);
		LocalTime toTime = LocalTime.of(16, 0, 0);

		Map<String, List<StockBar>> yesterdaysBars;
		Map<String, List<StockBar>> latestBars;
		Map<String, Double> priceChangePercentages;
		List<Map<String, ?>> priceChangeAndTradesList = new ArrayList<>();
		try {
			List<LocalDate> dates = this.getLastTwoTradingDays();
			LocalDate secondLastTradingDay = dates.getFirst();
			LocalDate lastTradingDay = dates.getLast();

			LocalTime lastTradingDayFromTime;
			LocalTime lastTradingDayEndTime;

			if (lastTradingDay.equals(nycTime.today())) {
				// All time comparisons now use NYC time
				boolean isAfterMarketOpen = nycTime.time().isAfter(LocalTime.of(9, 45));
				boolean isBeforeMarketClose = nycTime.time().isBefore(LocalTime.of(16, 15));

				// Intraday information
				if (isAfterMarketOpen && isBeforeMarketClose) {
					lastTradingDayFromTime = nycTime.time().minusMinutes(16);
					lastTradingDayEndTime = nycTime.time().minusMinutes(15);
				} else {
					lastTradingDayFromTime = LocalTime.of(15, 59, 0);
					lastTradingDayEndTime = LocalTime.of(16, 0, 0);
				}
			} else {
				lastTradingDayFromTime = LocalTime.of(15, 59, 0);
				lastTradingDayEndTime = LocalTime.of(16, 0, 0);
			}

			OffsetDateTime lastTradingDayOffsetStartTime = OffsetDateTime.of(lastTradingDay, lastTradingDayFromTime,
					nycTime.offset());
			OffsetDateTime lastTradingDayOffsetEndTime = OffsetDateTime.of(lastTradingDay, lastTradingDayEndTime,
					nycTime.offset());

			// Set the time range for yesterday's data
			OffsetDateTime yesterdaysOffsetStartTime = OffsetDateTime.of(secondLastTradingDay, fromTime, nycTime.offset());
			OffsetDateTime yesterdaysOffsetEndTime = OffsetDateTime.of(secondLastTradingDay, toTime, nycTime.offset());

			String tickers = String.join(",", tickersToGetDataFor);

			yesterdaysBars = this.alpacaAPI.marketData().stock().stockBars(tickers, "1Min", yesterdaysOffsetStartTime,
					yesterdaysOffsetEndTime, this.historicalDataLimit, StockAdjustment.ALL, null, StockFeed.IEX,
							this.currency, null, Sort.ASC).getBars();

			latestBars = this.alpacaAPI.marketData().stock().stockBars(tickers, "1Min", lastTradingDayOffsetStartTime,
					lastTradingDayOffsetEndTime, this.historicalDataLimit, StockAdjustment.ALL, null, StockFeed.IEX,
							this.currency, null, Sort.ASC).getBars();

			// The Stream API takes the keySet() (which returns a Set<String> in this case) for all the lambda operations.
			priceChangePercentages = yesterdaysBars.keySet().parallelStream()
					.filter(ticker -> yesterdaysBars.get(ticker) != null && !yesterdaysBars.get(ticker).isEmpty()
							&& latestBars.get(ticker) != null)
					.collect(Collectors.toMap(
							ticker -> ticker,
							ticker -> {
								Double yesterdaysClose = yesterdaysBars.get(ticker).getLast().getC();
								Double currentPrice = latestBars.get(ticker).getLast().getC();

								return new BigDecimal(Double.toString(((currentPrice - yesterdaysClose) / yesterdaysClose) * 100))
										.setScale(2, RoundingMode.HALF_UP).doubleValue();
							}
					));

			priceChangeAndTradesList.add(latestBars);
			priceChangeAndTradesList.add(priceChangePercentages);

		} catch (ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}

		return priceChangeAndTradesList;
	}

	/**
	 * Gets the historical 1-day stock bars for the given ticker.
	 * <ul>
	 * <li>If today is a trading day and it's during market hours (9:45 AM - 4:15 PM
	 * NYC):
	 * returns bars for today up to (current time - 15 min)</li>
	 * <li>If today is a trading day and it's after market close (4:15 PM+ NYC):
	 * returns all bars for today</li>
	 * <li>If today is a trading day and it's pre-market (before 9:45 AM NYC):
	 * returns all bars for the previous trading day</li>
	 * <li>If today is not a trading day (weekend/holiday):
	 * returns all bars for the most recent trading day</li>
	 * </ul>
	 *
	 * @param ticker The ticker whose 1-day bars are to be fetched.
	 *
	 * @return A list of StockBar objects representing the historical 1-day bars.
	 *         If the ticker is not found, null is returned.
	 */
	public List<StockBar> get1DHistoricalStockBars(String ticker) {
		NYCTimeInfo nycTime = this.getNYCTimeInfo();
		String timeFrame = "5Min";

		// Get the most recent trading day (accounts for pre-market, weekends, holidays)
		LocalDate tradingDay = this.getMostRecentTradingDay();

		// Determine the end time based on current time and whether today is the trading
		// day
		OffsetDateTime fetchEndTime;
		boolean isTodayTradingDay = tradingDay.equals(nycTime.today());
		boolean isDuringMarketHours = nycTime.time().isAfter(LocalTime.of(9, 45))
				&& nycTime.time().isBefore(LocalTime.of(16, 15));

		if (isTodayTradingDay && isDuringMarketHours) {
			// During market hours: fetch up to (now - 15 min) due to API restrictions
			fetchEndTime = nycTime.now().minusMinutes(15).toOffsetDateTime();
		} else {
			// After market close, pre-market, weekend, or holiday: fetch full day ending at
			// 4 PM
			fetchEndTime = OffsetDateTime.of(tradingDay, LocalTime.of(16, 0), nycTime.offset());
		}

		// Market opens at 9:30 AM
		OffsetDateTime fetchStartTime = OffsetDateTime.of(tradingDay, LocalTime.of(9, 30), nycTime.offset());

		return this.fetchHistoricalBars(ticker, fetchStartTime, fetchEndTime, timeFrame);
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
		LocalDate todayInNYC = this.getNYCTimeInfo().today();
		LocalDate nMonthsAgo = todayInNYC.minusMonths(1);
		long daysToSubtract = ChronoUnit.DAYS.between(nMonthsAgo, todayInNYC);

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
		LocalDate todayInNYC = this.getNYCTimeInfo().today();
		LocalDate nMonthsAgo = todayInNYC.minusMonths(3);
		long daysToSubtract = ChronoUnit.DAYS.between(nMonthsAgo, todayInNYC);

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
		LocalDate todayInNYC = this.getNYCTimeInfo().today();
		LocalDate nYearsAgo = todayInNYC.minusYears(1);
		long daysToSubtract = ChronoUnit.DAYS.between(nYearsAgo, todayInNYC);

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
		LocalDate todayInNYC = this.getNYCTimeInfo().today();
		LocalDate nYearsAgo = todayInNYC.minusYears(5);
		long daysToSubtract = ChronoUnit.DAYS.between(nYearsAgo, todayInNYC);

		return this.getAppropriateHistoricalBars(ticker, daysToSubtract, "7D");
	}

	/**
	 * Fetches the historical bars for the given ticker between the given time range
	 * divided by the given timeframe.
	 *
	 * @param ticker    The ticker whose historical bars are to be fetched.
	 * @param startTime The start time of the historical data to be fetched.
	 * @param endTime   The end time of the historical data to be fetched.
	 * @param timeFrame The timeframe (or interval) of the bars to be aggregated.
	 * @return A list of StockBar objects containing the historical bars for the
	 *         requested ticker.
	 */
	private List<StockBar> fetchHistoricalBars(String ticker, OffsetDateTime startTime, OffsetDateTime endTime,
			String timeFrame) {
		List<StockBar> historicalBars = new ArrayList<>();

		try {
			historicalBars = this.alpacaAPI.marketData().stock().stockBarSingle(
					ticker, timeFrame, startTime, endTime,
					historicalDataLimit, StockAdjustment.ALL, null,
					StockFeed.SIP, currency, null, Sort.ASC).getBars();
		} catch (ApiException e) {
			System.out.println(e.getCode() + "\n" + e.getMessage());
		}

		return historicalBars;
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
		NYCTimeInfo nycTime = this.getNYCTimeInfo();
		LocalDate startDay = nycTime.today().minusDays(daysToSubtract);

		OffsetDateTime startTime = OffsetDateTime.of(startDay, LocalTime.of(0, 0, 0), nycTime.offset());
		OffsetDateTime endTime = OffsetDateTime.of(nycTime.today(), LocalTime.of(23, 59, 59), nycTime.offset());

		return this.fetchHistoricalBars(ticker, startTime, endTime, timeFrame);
	}

	/**
	 * Gets the most recent trading day for intraday data, accounting for pre-market hours.
	 * Uses the cached market calendar.
	 * <ul>
	 *   <li>If today is a trading day AND it's before 9:45 AM NYC time → returns previous trading day</li>
	 *   <li>If today is a trading day AND it's 9:45 AM or later → returns today</li>
	 *   <li>If today is not a trading day (weekend/holiday) → returns most recent trading day</li>
	 * </ul>
	 *
	 * @return A LocalDate object representing the appropriate trading day for intraday data.
	 */
	private LocalDate getMostRecentTradingDay() {
		NYCTimeInfo nycTime = this.getNYCTimeInfo();
		List<Calendar> marketCalendar = this.getMarketCalendar();

		if (marketCalendar == null || marketCalendar.isEmpty()) {
			return null;
		}

		Calendar lastTradingDayCalendar = marketCalendar.getLast();
		boolean isTodayTradingDay = lastTradingDayCalendar.getDate().equals(nycTime.today().toString());
		boolean isBeforeMarketOpen = nycTime.time().isBefore(LocalTime.of(9, 45));

		Calendar targetDay;
		if (isTodayTradingDay && isBeforeMarketOpen) {
			// Pre-market: use previous trading day
			targetDay = marketCalendar.get(marketCalendar.size() - 2);
		} else {
			// During/after market hours or weekend/holiday: use most recent trading day
			targetDay = lastTradingDayCalendar;
		}

		String[] splitDate = targetDay.getDate().split("-");
		return LocalDate.of(Integer.parseUnsignedInt(splitDate[0]),
				Integer.parseUnsignedInt(splitDate[1]), Integer.parseUnsignedInt(splitDate[2]));
	}

	/**
	 * Gets the last two trading days using the cached market calendar.
	 *
	 * @return A list of LocalDate objects representing the last two trading days. The older date is given first and
	 * the later date is given second. If the request is done during pre-market hours, the last trading day is
	 * yesterday and the second last trading day is the day before yesterday. If an error occurs, null is returned.
	 */
	private List<LocalDate> getLastTwoTradingDays() {
		List<LocalDate> dates = new ArrayList<>();
		NYCTimeInfo nycTime = this.getNYCTimeInfo();
		List<Calendar> marketCalendar = this.getMarketCalendar();

		if (marketCalendar == null || marketCalendar.size() < 3) {
			return null;
		}

		// If today (in NYC) is the most recent trading day, and it's before 9:45 AM ET,
		// the data must be for yesterday and day before yesterday.
		boolean isTodayLastTradingDay = marketCalendar.getLast().getDate().equals(nycTime.today().toString());
		boolean isBeforeMarketOpen = nycTime.time().isBefore(LocalTime.of(9, 45));

		Calendar secondLastTradingDay;
		Calendar lastTradingDay;
		if (isTodayLastTradingDay && isBeforeMarketOpen) {
			secondLastTradingDay = marketCalendar.get(marketCalendar.size() - 3);
			lastTradingDay = marketCalendar.get(marketCalendar.size() - 2);
		} else {
			secondLastTradingDay = marketCalendar.get(marketCalendar.size() - 2);
			lastTradingDay = marketCalendar.getLast();
		}

		String[] splitSecondLast = secondLastTradingDay.getDate().split("-");
		String[] splitLast = lastTradingDay.getDate().split("-");

		dates.add(LocalDate.of(Integer.parseUnsignedInt(splitSecondLast[0]),
				Integer.parseUnsignedInt(splitSecondLast[1]), Integer.parseUnsignedInt(splitSecondLast[2])));

		dates.add(LocalDate.of(Integer.parseUnsignedInt(splitLast[0]),
				Integer.parseUnsignedInt(splitLast[1]), Integer.parseUnsignedInt(splitLast[2])));

		return dates;
	}
}
