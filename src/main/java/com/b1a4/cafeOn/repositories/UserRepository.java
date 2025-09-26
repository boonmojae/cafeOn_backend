package com.b1a4.cafeOn.repositories;

import com.b1a4.cafeOn.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Boolean existsByEmail(String email);
    UserEntity findByEmail(String email);
    UserEntity findByEmailAndPassword(String email, String password);
}