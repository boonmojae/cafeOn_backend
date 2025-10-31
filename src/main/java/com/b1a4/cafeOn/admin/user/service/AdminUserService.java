package com.b1a4.cafeOn.admin.user.service;

import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import com.b1a4.cafeOn.admin.penalty.repository.PenaltyRepository;
import com.b1a4.cafeOn.admin.user.dto.AdminPenaltyItemDTO;
import com.b1a4.cafeOn.admin.user.dto.AdminUserDetailDTO;
import com.b1a4.cafeOn.admin.user.dto.AdminUserListItemDTO;
import com.b1a4.cafeOn.admin.user.repository.AdminUserRepository;
import com.b1a4.cafeOn.common.api.ApiResponse;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminUserRepository userRepository;
    private final PenaltyRepository penaltyRepository;

    // 회원 목록 조회
    public ApiResponse<Page<AdminUserListItemDTO>> list(String status, String search, Pageable pageable) {
        UserStatus statusEnum = switch (status == null ? "all" : status.toLowerCase()) {
            case "normal" -> UserStatus.ACTIVE;
            case "suspended" -> UserStatus.SUSPENDED;
            default -> null;
        };

        String kw = (search == null || search.isBlank()) ? null : search.trim();

        Page<UserEntity> page = userRepository.searchUsers(statusEnum, kw, pageable);

        List<String> ids = page.getContent().stream().map(UserEntity::getUserId).toList();
        Map<String, Long> counts = ids.isEmpty()
                ? Map.of()
                : penaltyRepository.countGroupByUserId(ids); // PenaltyRepository에 추가된 다건 집계 메서드

        List<AdminUserListItemDTO> content = page.getContent().stream()
                .map(u -> AdminUserListItemDTO.builder()
                        .id(u.getUserId())
                        .name(u.getName() != null ? u.getName().trim() : null)
                        .email(u.getEmail())
                        .status(u.getStatus().name())
                        .penaltyCount(counts.getOrDefault(u.getUserId(), 0L))
                        .build())
                .toList();

        Page<AdminUserListItemDTO> dtoPage = new PageImpl<>(content, pageable, page.getTotalElements());

        return ApiResponse.<Page<AdminUserListItemDTO>>builder()
                .message("회원 목록 조회 완료")
                .data(dtoPage)
                .build();
    }

    // 회원 상세 조회
    public ApiResponse<AdminUserDetailDTO> detail(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

        long count = penaltyRepository.countByUser_UserId(userId);

        List<PenaltyEntity> penaltyEntities =
                penaltyRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                        .getContent();

        List<AdminPenaltyItemDTO> penalties = penaltyEntities.stream()
                .map(AdminPenaltyItemDTO::fromEntity)
                .collect(Collectors.toList());

        AdminUserDetailDTO dto = AdminUserDetailDTO.builder()
                .id(user.getUserId())
                .nickname(user.getNickname())
                .name(user.getName() != null ? user.getName().trim() : null)
                .email(user.getEmail())
                .status(user.getStatus().name())
                .penaltyCount(count)
                .penalties(penalties)
                .build();

        return ApiResponse.<AdminUserDetailDTO>builder()
                .message("회원 상세 조회 완료")
                .data(dto)
                .build();
    }
}
