package com.b1a4.cafeOn.admin.penalty.dto;

import com.b1a4.cafeOn.admin.penalty.enums.ReasonCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyRequestDTO {

    private String reason;
    private ReasonCode reasonCode; // "DISCOMFORT", "AI", "AD"

    private String duration; // SUSPEND일 때만 사용
}
