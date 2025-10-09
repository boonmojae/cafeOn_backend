package com.b1a4.cafeOn.user.repository;

import com.b1a4.cafeOn.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
//    find.. 등 조회(read) 전용.
//    update 전용 메서드는 없음 -> Service 레벨에서 엔티티 조작 (find -> set -> save)
//      ㄴ@Query없이 직관적, 디버깅 쉬움
//      ㄴJPA가 자동으로 트랜잭션, dirty checking 처리
//      ㄴ실무에서 비즈니스 로직을 "Service단에서 명시적으로 표현"하기 좋음
    
    Boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndPassword(String email, String password);
    Optional<UserEntity> findByRefreshToken(String refreshToken);
}