package com.b1a4.cafeOn.cafe.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CafeNearbyResponse {
    private List<CafeDTO> cafes;
}