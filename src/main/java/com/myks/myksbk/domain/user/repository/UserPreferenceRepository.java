package com.myks.myksbk.domain.user.repository;

import com.myks.myksbk.domain.user.domain.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(Long userId);
}