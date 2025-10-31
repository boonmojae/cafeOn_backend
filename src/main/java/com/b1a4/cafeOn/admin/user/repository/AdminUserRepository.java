package com.b1a4.cafeOn.admin.user.repository;

import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<UserEntity, String> {

    @Query("""
       SELECT u
       FROM UserEntity u
       WHERE u.role = com.b1a4.cafeOn.user.enums.UserRole.USER
         AND (:status IS NULL OR u.status = :status)
         AND (:kw IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :kw, '%')))
       """)
    Page<UserEntity> searchUsers(@Param("status") UserStatus status,
                                 @Param("kw") String kw,
                                 Pageable pageable);
}
