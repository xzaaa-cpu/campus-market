package com.campus.market.config;

import com.campus.market.common.BizException;

/**
 * 当前登录用户上下文（ThreadLocal）。
 */
public class CurrentUser {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void set(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 取当前登录用户，未登录直接抛 401。
     */
    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BizException(401, "请先登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
