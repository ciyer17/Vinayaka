package com.iyer.vinayaka.repository;

import com.iyer.vinayaka.entities.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer> {
}
