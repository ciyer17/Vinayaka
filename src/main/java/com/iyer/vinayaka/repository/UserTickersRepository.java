package com.iyer.vinayaka.repository;

import com.iyer.vinayaka.entities.UserTickers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTickersRepository extends JpaRepository<UserTickers, String> {
	List<UserTickers> findAllByFavorite(boolean favorite);
	
	List<UserTickers> findAllByExchange(String exchange);
	
	List<UserTickers> findAllByNameLike(String name);
}
