package com.iyer.vinayaka.repository;

import com.iyer.vinayaka.entities.UserTickers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTickersRepository extends JpaRepository<UserTickers, String> {
	List<UserTickers> findAllByFavorite(boolean favorite);
	
	List<UserTickers> findAllByExchange(String exchange);
	
	List<UserTickers> findAllByNameLike(String name);
}
