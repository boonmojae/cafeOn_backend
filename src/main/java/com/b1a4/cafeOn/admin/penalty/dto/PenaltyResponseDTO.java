package com.b1a4.cafeOn.admin.penalty.dto;

import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyResponseDTO {

    private Long penaltyId;
    private String penaltyType;
    private String reason;
    private String status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String adminNickname;
    private LocalDateTime createdAt;

    public static PenaltyResponseDTO fromEntity(PenaltyEntity entity) {
        return PenaltyResponseDTO.builder()
                .penaltyId(entity.getPenaltyId())
                .penaltyType(entity.getPenaltyType().name())
                .reason(entity.getReason())
                .status(entity.getStatus().name())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .adminNickname(entity.getAdmin().getNickname())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
