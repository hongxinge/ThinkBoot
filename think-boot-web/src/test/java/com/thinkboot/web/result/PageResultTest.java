package com.thinkboot.web.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    @DisplayName("PageResult.of() with valid data should set all fields correctly")
    void ofWithValidData() {
        List<String> records = Arrays.asList("item1", "item2", "item3");
        long total = 100L;
        long current = 1L;
        long size = 10L;

        PageResult<String> result = PageResult.of(records, total, current, size);

        assertNotNull(result);
        assertEquals(records, result.getRecords());
        assertEquals(100L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertEquals(10L, result.getPages());
        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("PageResult.of() with null records should return empty list")
    void ofWithNullRecords() {
        PageResult<String> result = PageResult.of(null, 50L, 1L, 10L);

        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
        assertEquals(50L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(10L, result.getSize());
        assertEquals(5L, result.getPages());
        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("PageResult.of() with empty records should set hasMore to false when at boundary")
    void ofWithEmptyRecords() {
        PageResult<String> result = PageResult.of(Collections.emptyList(), 0L, 1L, 10L);

        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
        assertEquals(0L, result.getTotal());
        assertEquals(0L, result.getPages());
        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore should be false when current * size >= total")
    void hasMoreAtBoundary() {
        PageResult<String> result = PageResult.of(
                Arrays.asList("item1", "item2"),
                20L,
                2L,
                10L
        );

        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore should be false when on last page")
    void hasMoreOnLastPage() {
        PageResult<String> result = PageResult.of(
                Arrays.asList("item1", "item2", "item3", "item4", "item5"),
                25L,
                5L,
                5L
        );

        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore should be true when more data exists")
    void hasMoreWhenMoreDataExists() {
        PageResult<String> result = PageResult.of(
                Arrays.asList("item1", "item2"),
                25L,
                1L,
                10L
        );

        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("Pagination math: pages should be ceiling of total/size")
    void paginationMathPages() {
        PageResult<String> result1 = PageResult.of(Collections.emptyList(), 100L, 1L, 10L);
        assertEquals(10L, result1.getPages());

        PageResult<String> result2 = PageResult.of(Collections.emptyList(), 101L, 1L, 10L);
        assertEquals(11L, result2.getPages());

        PageResult<String> result3 = PageResult.of(Collections.emptyList(), 99L, 1L, 10L);
        assertEquals(10L, result3.getPages());
    }

    @Test
    @DisplayName("Pages should be 0 when size is 0")
    void paginationMathZeroSize() {
        PageResult<String> result = PageResult.of(Collections.emptyList(), 100L, 1L, 0L);

        assertEquals(0L, result.getPages());
    }

    @Test
    @DisplayName("hasMore should be true on first page when total > size")
    void hasMoreOnFirstPage() {
        PageResult<String> result = PageResult.of(
                Collections.nCopies(10, "item"),
                100L,
                1L,
                10L
        );

        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore should be false when current * size exceeds total")
    void hasMoreBeyondTotal() {
        PageResult<String> result = PageResult.of(
                Collections.emptyList(),
                15L,
                2L,
                10L
        );

        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("PageResult constructor with records and total only")
    void constructorWithRecordsAndTotal() {
        List<String> records = Arrays.asList("a", "b", "c");
        long total = 10L;

        PageResult<String> result = new PageResult<>(records, total);

        assertEquals(records, result.getRecords());
        assertEquals(10L, result.getTotal());
        assertEquals(1L, result.getCurrent());
        assertEquals(3L, result.getSize());
        assertEquals(4L, result.getPages());
        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("PageResult constructor with null records should use empty list")
    void constructorWithNullRecords() {
        PageResult<String> result = new PageResult<>(null, 10L);

        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
        assertEquals(0L, result.getSize());
        assertEquals(0L, result.getPages());
        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("PageResult default constructor should create empty object")
    void defaultConstructor() {
        PageResult<String> result = new PageResult<>();

        assertNull(result.getRecords());
        assertEquals(0L, result.getTotal());
        assertEquals(0L, result.getCurrent());
        assertEquals(0L, result.getSize());
        assertEquals(0L, result.getPages());
        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore boundary: exact match current * size == total")
    void hasMoreExactBoundary() {
        PageResult<String> result = PageResult.of(
                Collections.nCopies(10, "item"),
                20L,
                2L,
                10L
        );

        assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("hasMore boundary: one item over boundary")
    void hasMoreOneOverBoundary() {
        PageResult<String> result = PageResult.of(
                Collections.nCopies(10, "item"),
                21L,
                2L,
                10L
        );

        assertTrue(result.isHasMore());
    }
}
