package com.thinkboot.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {

    @Test
    void testObjectMapperCanSerialize() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder().build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        TestObject testObj = new TestObject("test", 123);
        assertDoesNotThrow(() -> objectMapper.writeValueAsString(testObj));
    }

    @Test
    void testGenericJackson2JsonRedisSerializerCreation() {
        ObjectMapper objectMapper = new ObjectMapper();
        assertDoesNotThrow(() -> new GenericJackson2JsonRedisSerializer(objectMapper));
    }

    static class TestObject {
        private String name;
        private int value;

        public TestObject() {
        }

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
