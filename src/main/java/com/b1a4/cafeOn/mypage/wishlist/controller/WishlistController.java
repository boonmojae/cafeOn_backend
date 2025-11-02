package com.b1a4.cafeOn.mypage.wishlist.controller;

import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistListResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.enums.WishlistCategory;
import com.b1a4.cafeOn.mypage.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
            summary = "위시리스트 추가/제거",
            description = """
        특정 카페를 위시리스트에 추가하거나 제거합니다.
        이미 존재하면 제거되고, 존재하지 않으면 추가됩니다.
        카테고리는 hideout/work/atmosphere/taste/planned 중 하나를 선택합니다.
        """
    )
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
    @Operation(
            summary = "위시리스트 해제",
            description = "특정 카페를 지정한 카테고리에서 위시리스트 해제합니다."
    )
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
    @Operation(
            summary = "내 위시리스트 목록 조회",
            description = "선택한 카테고리에 해당하는 내 위시리스트 목록을 페이징하여 조회합니다."
    )
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
    @Operation(
            summary = "내 위시리스트 카테고리 조회",
            description = "특정 카페가 어떤 카테고리에 속해 있는지 확인합니다."
    )

    public ResponseEntity<ApiResponse<List<WishlistCategory>>> getMyWishlistCategories(
            @AuthenticationPrincipal String userId,
            @PathVariable("cafeId") Long cafeId
    ) {
        ApiResponse<List<WishlistCategory>> response =
                wishlistService.getMyCategories(userId, cafeId);
        return ResponseEntity.ok(response);
    }
}
