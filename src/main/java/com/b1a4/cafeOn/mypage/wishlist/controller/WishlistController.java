package com.b1a4.cafeOn.mypage.wishlist.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistListResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.enums.WishlistCategory;
import com.b1a4.cafeOn.mypage.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/my/wishlist", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Wishlist", description = "위시리스트 관련 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping(path = "/{cafeId}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> toggleWishlist(
            @AuthenticationPrincipal String userId,
            @PathVariable("cafeId") Long cafeId,
            @RequestParam("category") WishlistCategory category
    ) {
        ApiResponse<WishlistResponseDTO> response =
                wishlistService.toggle(userId, cafeId, category.name());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cafeId}")
    public ResponseEntity<ApiResponse<WishlistResponseDTO>> unwish(
            @AuthenticationPrincipal String userId,
            @PathVariable("cafeId") Long cafeId,
            @RequestParam("category") WishlistCategory category
    ) {
        ApiResponse<WishlistResponseDTO> response =
                wishlistService.unwish(userId, cafeId, category);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WishlistListResponseDTO>>> getWishlistList(
            @AuthenticationPrincipal String userId,
            @RequestParam("category") WishlistCategory category,
            @ParameterObject Pageable pageable
    ) {
        ApiResponse<Page<WishlistListResponseDTO>> response =
                wishlistService.list(userId, category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cafeId}")
    public ResponseEntity<ApiResponse<List<WishlistCategory>>> getMyWishlistCategories(
            @AuthenticationPrincipal String userId,
            @PathVariable("cafeId") Long cafeId
    ) {
        ApiResponse<List<WishlistCategory>> response =
                wishlistService.getMyCategories(userId, cafeId);
        return ResponseEntity.ok(response);
    }
}
