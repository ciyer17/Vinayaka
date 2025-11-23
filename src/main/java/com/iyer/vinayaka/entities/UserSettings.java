package com.iyer.vinayaka.entities;

import com.iyer.vinayaka.util.UIUtils;
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
	@Column(name = "api_key", nullable = false, length = UIUtils.API_KEY_LEN)
	private String API_KEY;

	@NonNull
	@Column(name = "api_secret", nullable = false, length = UIUtils.API_SECRET_LEN)
	private String API_SECRET;

	@NonNull
	@Column(nullable = false)
	private Integer refresh_interval = 10;

	@NonNull
	@Column(nullable = false)
	private Boolean dark_mode = true;

	@NonNull
	@Column(nullable = false)
	private String timezone = "America/New_York";
}
