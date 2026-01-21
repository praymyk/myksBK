package com.myks.myksbk.domain.user.repository;

import com.myks.myksbk.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.account = :loginId OR u.email = :loginId")
    Optional<User> findByAccountOrEmail(@Param("loginId") String loginId);
}
