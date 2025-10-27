package com.b1a4.cafeOn.common;

import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.enums.UserStatus;

public final class DisplayMasking {
    private static final String UNKNOWN = "(알 수 없음)";
    private DisplayMasking() {}

    public static boolean isDeleted(UserEntity user) {
        return user == null || user.getStatus() == UserStatus.DELETED;
    }
    public static String nicknameOf(UserEntity user) {
        return isDeleted(user) ? UNKNOWN : user.getNickname();
    }
    public static String profileUrlOf(UserEntity user) {
        return isDeleted(user) ? null : user.getProfileImage();
    }
    // 외부 응답에서 탈퇴자 ID 노출 정책: 숨기려면 true일 때 null을 반환하도록
    public static String safeUserId(UserEntity user, boolean hideIfDeleted) {
        if (user == null) return null;
        if (hideIfDeleted && isDeleted(user)) return null;
        return user.getUserId();
    }
}