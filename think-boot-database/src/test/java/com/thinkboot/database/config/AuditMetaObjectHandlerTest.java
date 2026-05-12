package com.thinkboot.database.config;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditMetaObjectHandlerTest {

    private AuditMetaObjectHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuditMetaObjectHandler();
    }

    @Test
    @DisplayName("insertFill should set all audit fields on entity")
    void insertFillSetsAllFields() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        handler.insertFill(metaObject);

        assertNotNull(entity.getCreatedTime());
        assertNotNull(entity.getUpdatedTime());
        assertEquals("system", entity.getCreatedBy());
        assertEquals("system", entity.getUpdatedBy());
        assertTrue(entity.getCreatedTime().isAfter(before) || entity.getCreatedTime().isEqual(before));
        assertTrue(entity.getCreatedTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("updateFill should only set updated fields")
    void updateFillOnlySetsUpdatedFields() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.updateFill(metaObject);

        assertNotNull(entity.getUpdatedTime());
        assertEquals("system", entity.getUpdatedBy());
        assertNull(entity.getCreatedTime());
        assertNull(entity.getCreatedBy());
    }

    @Test
    @DisplayName("updateFill should set current time")
    void updateFillSetsCurrentTime() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        handler.updateFill(metaObject);

        assertNotNull(entity.getUpdatedTime());
        assertTrue(entity.getUpdatedTime().isAfter(before) || entity.getUpdatedTime().isEqual(before));
    }

    @Test
    @DisplayName("getCurrentUser should return 'system' by default")
    void getCurrentUserReturnsSystem() {
        assertEquals("system", handler.getCurrentUser());
    }

    @Test
    @DisplayName("insertFill should handle entity without audit fields gracefully")
    void insertFillWithoutAuditFields() {
        NonAuditEntity entity = new NonAuditEntity();
        entity.setName("test");
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        assertDoesNotThrow(() -> handler.insertFill(metaObject));

        // No audit fields should be set since entity doesn't have them
        assertNull(entity.getId());
    }

    @Test
    @DisplayName("updateFill should handle entity without audit fields gracefully")
    void updateFillWithoutAuditFields() {
        NonAuditEntity entity = new NonAuditEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        assertDoesNotThrow(() -> handler.updateFill(metaObject));
    }

    @Test
    @DisplayName("insertFill should set LocalDateTime type for time fields")
    void insertFillSetsCorrectTimeType() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        handler.insertFill(metaObject);

        assertInstanceOf(LocalDateTime.class, entity.getCreatedTime());
        assertInstanceOf(LocalDateTime.class, entity.getUpdatedTime());
    }

    @Test
    @DisplayName("insertFill should set String type for user fields")
    void insertFillSetsCorrectUserType() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);
        handler.insertFill(metaObject);

        assertInstanceOf(String.class, entity.getCreatedBy());
        assertInstanceOf(String.class, entity.getUpdatedBy());
    }

    @Test
    @DisplayName("insertFill should skip fields without setters")
    void insertFillSkipsMissingFields() {
        PartialEntityWithAll entity = new PartialEntityWithAll();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        assertNotNull(entity.getCreatedTime());
        assertNotNull(entity.getUpdatedTime());
        assertEquals("system", entity.getCreatedBy());
        assertEquals("system", entity.getUpdatedBy());
    }

    @Test
    @DisplayName("insertFill and updateFill should work together")
    void insertAndUpdateFillWorkTogether() {
        TestEntity entity = new TestEntity();
        MetaObject insertMetaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(insertMetaObject);

        assertNotNull(entity.getCreatedTime());
        assertNotNull(entity.getUpdatedTime());
        assertEquals("system", entity.getCreatedBy());
        assertEquals("system", entity.getUpdatedBy());

        LocalDateTime originalUpdateTime = entity.getUpdatedTime();

        MetaObject updateMetaObject = SystemMetaObject.forObject(entity);
        handler.updateFill(updateMetaObject);

        assertTrue(entity.getUpdatedTime().isAfter(originalUpdateTime) ||
                   entity.getUpdatedTime().isEqual(originalUpdateTime));
    }

    @Test
    @DisplayName("insertFill time fields should be close together")
    void insertFillTimeFieldsClose() {
        TestEntity entity = new TestEntity();
        MetaObject metaObject = SystemMetaObject.forObject(entity);

        handler.insertFill(metaObject);

        long diff = java.time.Duration.between(entity.getCreatedTime(), entity.getUpdatedTime()).abs().toMillis();
        assertTrue(diff < 1000, "Time difference should be less than 1 second, but was " + diff + "ms");
    }

    private static class TestEntity {
        private Long id;
        private String name;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private String createdBy;
        private String updatedBy;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        public LocalDateTime getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }

    private static class NonAuditEntity {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private static class PartialEntity {
        private LocalDateTime createdTime;
        private String createdBy;

        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }

    private static class PartialEntityWithAll {
        private LocalDateTime createdTime;
        private String createdBy;
        private LocalDateTime updatedTime;
        private String updatedBy;

        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public LocalDateTime getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    }
}
