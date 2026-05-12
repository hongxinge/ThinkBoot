package com.thinkboot.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ThinkBootApplicationTest {

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
    }
}
