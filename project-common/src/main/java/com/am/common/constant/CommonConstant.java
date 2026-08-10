package com.am.common.constant;

public final class CommonConstant {

    private CommonConstant() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REDIS_TOKEN_PREFIX = "am:token:";
    public static final String REDIS_CONFIG_PREFIX = "am:config:";
    public static final String REDIS_AI_CONTEXT_PREFIX = "am:ai:context:";
    public static final String REDIS_HOME_PAGE_KEY = "am:home:page";

    public static final int MAX_AI_CONTEXT_ROUNDS = 20;

    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "webp"};
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static final String USER_STATUS_PENDING = "PENDING";
    public static final String USER_STATUS_ENABLED = "ENABLED";
    public static final String USER_STATUS_DISABLED = "DISABLED";

    public static final String ROLE_GUEST = "GUEST";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String INITIAL_PASSWORD = "33550336";
}
