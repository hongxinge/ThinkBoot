package com.thinkboot.database.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    @Test
    void testBaseEntityDefaultValues() {
        BaseEntity entity = new BaseEntity();
        assertNull(entity.getCreatedBy());
        assertNull(entity.getCreatedTime());
        assertNull(entity.getUpdatedBy());
        assertNull(entity.getUpdatedTime());
    }

    @Test
    void testBaseEntitySettersAndGetters() {
        BaseEntity entity = new BaseEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setCreatedBy("admin");
        entity.setCreatedTime(now);
        entity.setUpdatedBy("editor");
        entity.setUpdatedTime(now.plusHours(1));

        assertEquals("admin", entity.getCreatedBy());
        assertEquals(now, entity.getCreatedTime());
        assertEquals("editor", entity.getUpdatedBy());
        assertEquals(now.plusHours(1), entity.getUpdatedTime());
    }

    @Test
    void testBaseEntityImplementsSerializable() {
        BaseEntity entity = new BaseEntity();
        assertInstanceOf(java.io.Serializable.class, entity);
    }

    @Test
    void testBaseEntityEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();

        BaseEntity entity1 = new BaseEntity();
        entity1.setCreatedBy("admin");
        entity1.setCreatedTime(now);

        BaseEntity entity2 = new BaseEntity();
        entity2.setCreatedBy("admin");
        entity2.setCreatedTime(now);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testBaseEntityToString() {
        BaseEntity entity = new BaseEntity();
        entity.setCreatedBy("admin");

        String toString = entity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("admin"));
    }
}
