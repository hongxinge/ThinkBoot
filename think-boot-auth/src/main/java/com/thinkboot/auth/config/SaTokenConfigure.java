package com.thinkboot.auth.config;

import cn.dev33.satoken.config.SaTokenConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "think-boot.auth", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "sa-token")
public class SaTokenConfigure {

    private String tokenName = "Authorization";
    private int timeout = 7200;
    private int activeTimeout = -1;
    private boolean isConcurrent = true;
    private boolean isShare = true;
    private String tokenStyle = "uuid";
    private boolean isLog = false;

    @Bean
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName(tokenName);
        config.setTimeout(timeout);
        config.setActiveTimeout(activeTimeout);
        config.setIsConcurrent(isConcurrent);
        config.setIsShare(isShare);
        config.setTokenStyle(tokenStyle);
        config.setIsLog(isLog);
        return config;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setActiveTimeout(int activeTimeout) {
        this.activeTimeout = activeTimeout;
    }

    public void setIsConcurrent(boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
    }

    public void setIsShare(boolean isShare) {
        this.isShare = isShare;
    }

    public void setTokenStyle(String tokenStyle) {
        this.tokenStyle = tokenStyle;
    }

    public void setIsLog(boolean isLog) {
        this.isLog = isLog;
    }
}