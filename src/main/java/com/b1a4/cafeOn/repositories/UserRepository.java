package com.b1a4.cafeOn.repositories;

import com.b1a4.cafeOn.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndPassword(String email, String password);
    Optional<UserEntity> findByRefreshToken(String refreshToken);
}