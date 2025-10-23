package com.b1a4.cafeOn.mypage.wishlist.dto;

public record WishlistListResponseDTO(
        Long id,
        Long cafeId,
        String name,
        String category
) { }
