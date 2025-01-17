package com.iyer.vinayaka.service;

import com.iyer.vinayaka.entities.UserTickers;
import com.iyer.vinayaka.repository.UserTickersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTickersService {
	private final UserTickersRepository userTickersRepository;
	
	@Autowired
	public UserTickersService(UserTickersRepository repository) {
		this.userTickersRepository = repository;
	}
	
	/**
	 * Adds the specified ticker to the user's list of tickers.
	 *
	 * @param ticker The ticker to add.
	 * @return The saved ticker object.
	 */
	@Transactional
	public UserTickers addTicker(UserTickers ticker) {
		return this.userTickersRepository.save(ticker);
	}
	
	/**
	 * Retrieves all symbols stored by the user in ascending order, sorted by symbol.
	 *
	 * @return A list of all tickers sorted by symbol.
	 */
	public List<UserTickers> getAllTickersSortedBySymbol() {
		Sort order = Sort.by(Sort.Order.asc("symbol"));
		return this.userTickersRepository.findAll(order);
	}
	
	/**
	 * Retrieves the ticker with the specified symbol.
	 *
	 * @param symbol The symbol of the ticker to retrieve.
	 * @return The ticker object of the specified symbol, and null if the symbol doesn't exist
	 * 			in the database.
	 */
	public UserTickers getTicker(String symbol) {
		return this.userTickersRepository.findById(symbol).orElse(null);
	}
	
	/**
	 * Retrieves all tickers that the user has favorited.
	 *
	 * @return The list of all tickers that the user has favorited.
	 */
	public List<UserTickers> getFavoriteTickers() {
		return this.userTickersRepository.findAllByFavorite(true);
	}
	
	/**
	 * Retrieves all tickers that the user has not favorited.
	 *
	 * @return The list of all tickers that the user has not favorited.
	 */
	public List<UserTickers> getNonFavoriteTickers() {
		return this.userTickersRepository.findAllByFavorite(false);
	}
	
	/**
	 * Retrieves all tickers that are on the specified exchange.
	 * Possible values: NYSE and NASDAQ
	 *
	 * @param exchange The exchange to retrieve tickers from.
	 * @return The list of all tickers on the specified exchange.
	 */
	public List<UserTickers> getTickersByExchange(String exchange) {
		return this.userTickersRepository.findAllByExchange(exchange);
	}
	
	/**
	 * Retrieves all tickers whose company name starts with the specified name.
	 *
	 * @param name The company name to search for.
	 * @return The list of all tickers whose company name starts with the specified name.
	 */
	public List<UserTickers> getTickersByName(String name) {
		return this.userTickersRepository.findAllByNameLike(name + "%");
	}
	
	/**
	 * Deletes the ticker with the specified symbol.
	 *
	 * @param symbol The symbol of the ticker to delete.
	 */
	@Transactional
	public void deleteTicker(String symbol) {
		this.userTickersRepository.deleteById(symbol);
	}
	
	/**
	 * Deletes all tickers stored by the user.
	 */
	@Transactional
	public void deleteAllTickers() {
		this.userTickersRepository.deleteAll();
	}
}
