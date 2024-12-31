package com.iyer.vinayaka.repository;

import com.iyer.vinayaka.entities.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer> {
}
