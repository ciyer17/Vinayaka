package com.iyer.vinayaka.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
public class UserSettings {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@NonNull
	@Column(name = "api_key", nullable = false, columnDefinition = "varchar(255)")
	private String API_KEY;
	
	@NonNull
	@Column(name = "api_secret", nullable = false, columnDefinition = "varchar(255)")
	private String API_SECRET;
	
	@NonNull
	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String password;
	
	@NonNull
	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String salt;
	
	@NonNull
	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String iv;
	
	@NonNull
	@Column(nullable = false, columnDefinition = "integer default 10")
	private Integer refresh_interval;
	
	@NonNull
	@Column(nullable = false)
	private Boolean dark_mode;
	
	@NonNull
	@Column(nullable = false, columnDefinition = "varchar(255) default 'America/Los_Angeles'")
	private String timezone;
}
