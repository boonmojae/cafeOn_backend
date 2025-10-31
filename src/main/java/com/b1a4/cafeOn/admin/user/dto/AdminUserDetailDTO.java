package com.b1a4.cafeOn.admin.user.dto;

import lombok.*;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserDetailDTO {
    private String id;
    private String nickname;
    private String name;
    private String email;
    private String status;
    private long penaltyCount;
    private List<AdminPenaltyItemDTO> penalties;
}
