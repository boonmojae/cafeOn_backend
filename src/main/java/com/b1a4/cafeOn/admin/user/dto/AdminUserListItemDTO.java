package com.b1a4.cafeOn.admin.user.dto;

import lombok.*;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserListItemDTO {
    private String id;
    private String name;
    private String email;
    private String status;
    private long penaltyCount;
}
