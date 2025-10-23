package com.b1a4.cafeOn.cafe.entity;

import com.b1a4.cafeOn.cafe.enums.CafeSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity // DB 구조와 같아야 함
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cafes")
public class CafeEntity {
    @Id
    @Column(name = "cafe_id", columnDefinition = "BIGINT")
    private Long cafeId;

    @Column(name = "kakao_id", columnDefinition = "VARCHAR(50)")
    private String kakaoId;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "address", columnDefinition = "VARCHAR(500)")
    private String address;

    @Column(name = "latitude", columnDefinition = "DECIMAL(20,15)")
    private BigDecimal latitude;

    @Column(name = "longitude", columnDefinition = "DECIMAL(20,15)")
    private BigDecimal longitude;

    @Column(name = "phone", columnDefinition = "VARCHAR(50)")
    private String phone;

    @Column(name = "open_hours", columnDefinition = "TEXT")
    private String open_hours;

    @Column(name = "avg_rating", columnDefinition = "DECIMAL(3,2)")
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.valueOf(0.00);

    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "kakao_url", columnDefinition = "VARCHAR(255)")
    private String kakaoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    @Builder.Default
    private CafeSource source = CafeSource.KAKAO;

    /* 나중에 크롤러나 관리자페이지 등에서 새로운카페가 추가되거나 기존 정보가 수정될 가능성이 생긴다면 이부분 활성화
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();   // todo: updatedAt 컬럼은 없으므로 생성필수
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }*/
}