package com.thinkboot.database.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 注意：框架不提供 @MapperScan 配置，开发者需在启动类自行配置扫描路径
 * 框架仅提供开箱即用的分页/乐观锁/防全表更新拦截器组合
 * 分页参数可通过 think-boot.database.pagination.* 配置
 */
@Configuration
@ConfigurationProperties(prefix = "think-boot.database")
public class MybatisPlusConfig {

    private Pagination pagination = new Pagination();

    /**
     * 分页拦截器配置（暴露可配置参数）
     *
     * 注意：MyBatis-Plus 原生的拦截器配置通过 mybatis-plus.global-config 或 mybatis-plus.configuration 配置
     * 此处仅增强：提供开箱即用的分页/乐观锁/防全表更新拦截器组合
     * 分页参数可通过 think-boot.database.pagination.* 配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setMaxLimit(pagination.getMaxLimit());
        paginationInterceptor.setOverflow(pagination.isOverflow());
        interceptor.addInnerInterceptor(paginationInterceptor);

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    @Bean
    public AuditMetaObjectHandler metaObjectHandler() {
        return new AuditMetaObjectHandler();
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public static class Pagination {
        private long maxLimit = 500L;
        private boolean overflow = true;

        public long getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(long maxLimit) {
            this.maxLimit = maxLimit;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public void setOverflow(boolean overflow) {
            this.overflow = overflow;
        }
    }
}
