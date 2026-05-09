package com.thinkboot.core.constant;

public final class RedisConstants {

    private RedisConstants() {
    }

    public static final String TOKEN_PREFIX = "token:";

    public static final String USER_INFO_PREFIX = "user:info:";

    public static final String CAPTCHA_PREFIX = "captcha:";

    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    public static final String LOCK_PREFIX = "lock:";

    public static final long DEFAULT_EXPIRE = 7200;

    public static final long CAPTCHA_EXPIRE = 300;
}