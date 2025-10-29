package com.b1a4.cafeOn.mypage.wishlist.service;

import com.b1a4.cafeOn.cafe.entity.CafeEntity;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistListResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.dto.WishlistResponseDTO;
import com.b1a4.cafeOn.mypage.wishlist.entity.WishlistEntity;
import com.b1a4.cafeOn.mypage.wishlist.enums.WishlistCategory;
import com.b1a4.cafeOn.mypage.wishlist.repository.WishlistRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public ApiResponse<WishlistResponseDTO> toggle(String userId, Long cafeId, String categoryRaw) {
        final WishlistCategory category = parseCategory(categoryRaw);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        // CafeRepository 없이 프록시 레퍼런스만 획득 (DB 조회 안 함)
        CafeEntity cafeRef = em.getReference(CafeEntity.class, cafeId);

        boolean exists = wishlistRepository
                .existsByUserUserIdAndCafeCafeIdAndCategory(userId, cafeId, category);
        boolean wished;

        try {
            if (exists) {
                wishlistRepository.deleteByUserUserIdAndCafeCafeIdAndCategory(userId, cafeId, category);
                wished = false;
            } else {
                WishlistEntity entity = WishlistEntity.builder()
                        .user(user)
                        .cafe(cafeRef)
                        .category(category)
                        .build();
                wishlistRepository.save(entity);
                wished = true;
            }
        } catch (DataIntegrityViolationException e) {
            throw new EntityNotFoundException("존재하지 않는 카페입니다.");
        }

        return ApiResponse.<WishlistResponseDTO>builder()
                .message("위시리스트가 반영되었습니다.")
                .data(new WishlistResponseDTO(cafeId, wished))
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<WishlistListResponseDTO>> list(
            String userId,
            WishlistCategory category,
            Pageable pageable
    ) {
        Page<WishlistListResponseDTO> page =
                wishlistRepository.findListByUserAndCategory(userId, category, pageable);

        return ApiResponse.<Page<WishlistListResponseDTO>>builder()
                .message("위시리스트 조회 완료")
                .data(page)
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<WishlistCategory>> getMyCategories(String userId, Long cafeId) {
        List<WishlistCategory> categories =
                wishlistRepository.findCategoriesByUserAndCafe(userId, cafeId);

        return ApiResponse.<List<WishlistCategory>>builder()
                .message("카테고리 조회 완료")
                .data(categories)
                .build();
    }

    private WishlistCategory parseCategory(String raw) {
        try {
            return WishlistCategory.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 category 값입니다: " + raw);
        }
    }
}
