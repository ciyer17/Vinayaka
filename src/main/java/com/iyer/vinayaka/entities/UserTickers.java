package com.iyer.vinayaka.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "user_tickers")
@Data
@AllArgsConstructor
public class UserTickers {
	@Id
	@Column(nullable = false, length = 30)
	private String symbol;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, length = 20)
	private String exchange;

	@Column(name = "is_favorite", nullable = false)
	private boolean favorite;

	public UserTickers() {}
}
