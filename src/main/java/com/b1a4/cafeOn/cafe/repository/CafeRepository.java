package com.b1a4.cafeOn.cafe.repository;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CafeRepository extends JpaRepository<CafeEntity, Long> {

    @Query("SELECT c.name FROM CafeEntity c WHERE c.cafeId =:cafeId")
    Optional<String> findNameById(@Param("cafeId") Long cafeId);
}
