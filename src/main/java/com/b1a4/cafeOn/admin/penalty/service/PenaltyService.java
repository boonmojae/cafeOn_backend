package com.b1a4.cafeOn.admin.penalty.service;

import com.b1a4.cafeOn.admin.penalty.dto.PenaltyRequestDTO;
import com.b1a4.cafeOn.admin.penalty.dto.PenaltyResponseDTO;
import com.b1a4.cafeOn.admin.penalty.entity.PenaltyEntity;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyStatus;
import com.b1a4.cafeOn.admin.penalty.enums.PenaltyType;
import com.b1a4.cafeOn.admin.penalty.enums.ReasonCode;
import com.b1a4.cafeOn.admin.penalty.repository.PenaltyRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;

    /**
     * 경고 부여 (POST /api/admin/users/{id}/penalty)
     */
    @Transactional
    public PenaltyResponseDTO giveWarning(String userId, String adminId, PenaltyRequestDTO dto, Long reportId) {
        UserEntity user = getUserOrThrow(userId);
        UserEntity admin = getUserOrThrow(adminId);

        // (선택) 신고 연동 시 중복 방지
        if (reportId != null && penaltyRepository.existsByReportId(reportId)) {
            throw new IllegalStateException("이미 해당 신고로 패널티가 생성되었습니다. reportId=" + reportId);
        }

        PenaltyEntity entity = PenaltyEntity.builder()
                .user(user)
                .admin(admin)
                .penaltyType(PenaltyType.WARNING)
                .reason(dto.getReason())
                .reasonCode(dto.getReasonCode()) // null 허용
                .status(PenaltyStatus.ACTIVE)
                .startsAt(null)
                .endsAt(null)
                .build();

        // (옵션) report_id 보관
        if (reportId != null) {
            entity.setReportId(reportId);
        }

        PenaltyEntity saved = penaltyRepository.save(entity);

        // users.penalty_count += 1
        incrementPenaltyCount(user);

        return PenaltyResponseDTO.fromEntity(saved);
    }

    /**
     * 정지 부여 (POST /api/admin/users/{id}/suspend)
     * dto.duration 예: "7d", "30d"
     */
    @Transactional
    public PenaltyResponseDTO suspend(String userId, String adminId, PenaltyRequestDTO dto, Long reportId) {
        UserEntity user = getUserOrThrow(userId);
        UserEntity admin = getUserOrThrow(adminId);

        if (dto.getDuration() == null || dto.getDuration().isBlank()) {
            throw new IllegalArgumentException("duration은 필수입니다. 예: 7d, 30d");
        }

        // (선택) 신고 연동 시 중복 방지
        if (reportId != null && penaltyRepository.existsByReportId(reportId)) {
            throw new IllegalStateException("이미 해당 신고로 패널티가 생성되었습니다. reportId=" + reportId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusDays(parseDurationToDays(dto.getDuration()));

        PenaltyEntity entity = PenaltyEntity.builder()
                .user(user)
                .admin(admin)
                .penaltyType(PenaltyType.SUSPEND)
                .reason(Optional.ofNullable(dto.getReason()).orElse("관리자 정지 조치"))
                .reasonCode(dto.getReasonCode()) // null 허용
                .status(PenaltyStatus.ACTIVE)
                .startsAt(now)
                .endsAt(endsAt)
                .build();

        // (옵션) report_id 보관
        if (reportId != null) {
            entity.setReportId(reportId);
        }

        PenaltyEntity saved = penaltyRepository.save(entity);

        // users.penalty_count += 1
        incrementPenaltyCount(user);

        return PenaltyResponseDTO.fromEntity(saved);
    }

    /**
     * 정지 해제 (선택 API용) — 만료/관리자 해제
     */
    @Transactional
    public void revoke(Long penaltyId, String adminId, String reason) {
        PenaltyEntity penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new IllegalArgumentException("패널티를 찾을 수 없습니다. id=" + penaltyId));

        // 상태 변경만 — penalty_count는 이력 개념이라 감소하지 않음
        penalty.setStatus(PenaltyStatus.REVOKED);
        // (선택) 사유 남기고 싶으면 reason append
        if (reason != null && !reason.isBlank()) {
            String merged = (penalty.getReason() == null ? "" : penalty.getReason() + " | ") + "해제사유: " + reason;
            penalty.setReason(merged);
        }
        // 저장은 트랜잭션 종료 시 flush
    }

    /* -------------------- 내부 유틸 -------------------- */

    private UserEntity getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. userId=" + userId));
    }

    private void incrementPenaltyCount(UserEntity user) {
        Integer current = Optional.ofNullable(user.getPenaltyCount()).orElse(0);
        user.setPenaltyCount(current + 1);
        // dirty checking으로 자동 update
    }

    /**
     * "7d" / "30d" / "2w" / "1m" 형태 지원 (확장)
     * d: 일, w: 주, m: 30일 기준 월
     */
    private long parseDurationToDays(String duration) {
        String val = duration.trim().toLowerCase(Locale.ROOT);
        Pattern p = Pattern.compile("^(\\d+)\\s*([dwm])$");
        Matcher m = p.matcher(val);
        if (!m.matches()) {
            throw new IllegalArgumentException("duration 형식이 유효하지 않습니다. 예: 7d, 2w, 1m");
        }
        long n = Long.parseLong(m.group(1));
        char unit = m.group(2).charAt(0);
        return switch (unit) {
            case 'd' -> n;
            case 'w' -> n * 7;
            case 'm' -> n * 30; // 운영정책에 맞게 조정 가능
            default -> throw new IllegalArgumentException("지원하지 않는 단위: " + unit);
        };
    }
}
