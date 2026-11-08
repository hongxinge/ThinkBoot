package com.thinkboot.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端冒烟：覆盖登录、鉴权、异常映射、幂等与分布式锁。
 * 依赖本机 Redis；不可用时自动跳过，保证无中间件环境仍可完成构建。
 * 数据库使用 application-test.yml 的 H2 内存库与 schema-h2.sql。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSmokeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void requireRedis() {
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "本机 Redis 不可用，跳过集成冒烟");
        }
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        assertEquals(200, node.get("code").asInt(), "默认账号应能登录，BCrypt 哈希必须与示例口令匹配");
        return node.get("data").get("token").asText();
    }

    @Test
    @DisplayName("未登录访问受保护接口返回 401 且使用统一响应结构")
    void unauthenticatedReturns401() throws Exception {
        String body = mvc.perform(get("/api/auth/user/info"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(body);
        assertEquals(401, node.get("code").asInt());
        assertTrue(node.has("timestamp") && node.has("traceId"), "认证失败也必须返回 R 结构");
    }

    @Test
    @DisplayName("免密签发 Token 的示例端点已移除")
    void anonymousTokenEndpointRemoved() throws Exception {
        int statusCode = mvc.perform(post("/api/v1/auth/login")
                        .param("userId", "999")
                        .param("username", "attacker"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(200, statusCode, "不允许存在无密码校验即可签发 Token 的入口");
    }

    @Test
    @DisplayName("畸形 JSON 返回 400 而不是 500")
    void malformedJsonReturns400() throws Exception {
        String token = login();
        mvc.perform(post("/demo/lock")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("幂等提交第二次应被拒绝")
    void idempotentRejectsDuplicate() throws Exception {
        String token = login();
        String first = mvc.perform(post("/demo/submit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"smoke\":\"dup\"}"))
                .andReturn().getResponse().getContentAsString();
        assertEquals(200, mapper.readTree(first).get("code").asInt());

        String second = mvc.perform(post("/demo/submit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"smoke\":\"dup\"}"))
                .andReturn().getResponse().getContentAsString();
        assertNotEquals(200, mapper.readTree(second).get("code").asInt(), "幂等切面必须真实生效");
    }

    @Test
    @DisplayName("分布式锁 Lua 脚本可正常执行")
    void distributedLockSucceeds() throws Exception {
        String token = login();
        String body = mvc.perform(post("/demo/lock")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"SMOKE\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(200, mapper.readTree(body).get("code").asInt());
    }
}
