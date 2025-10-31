package com.b1a4.cafeOn.admin.user.dto;

import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminPenaltyItemDTO {
    private Long penaltyId;
    private String penaltyType;
    private String reason;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String adminNickname;
    private LocalDateTime createdAt;

    public static AdminPenaltyItemDTO fromEntity(PenaltyEntity e) {
        return AdminPenaltyItemDTO.builder()
                .penaltyId(e.getPenaltyId())
                .penaltyType(e.getPenaltyType().name())
                .reason(e.getReason())
                .status(e.getStatus().name())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .adminNickname(e.getAdmin() != null ? e.getAdmin().getNickname() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
