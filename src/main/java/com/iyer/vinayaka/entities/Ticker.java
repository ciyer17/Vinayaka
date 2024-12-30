package com.iyer.vinayaka.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ticker")
@Data
public class Ticker {
	@Id
	@Column(nullable = false, columnDefinition = "varchar(30)")
	private String symbol;
	
	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String name;
	
	@Column(nullable = false)
	private boolean is_favorite;
}
