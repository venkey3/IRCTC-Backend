package com.venkatesh.irctc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venkatesh.irctc.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
