package com.b1a4.cafeOn.Repository;

import com.b1a4.cafeOn.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public class UserRepository extends JpaRepository<UserEntity, String> {
}
