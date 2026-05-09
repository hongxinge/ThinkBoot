package com.thinkboot.database.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageQueryTest {

    @Test
    void testPageQueryDefaultValues() {
        PageQuery query = new PageQuery();
        assertEquals(1, query.getCurrent());
        assertEquals(10, query.getSize());
        assertNull(query.getOrderBy());
        assertNull(query.getOrderDirection());
    }

    @Test
    void testPageQuerySettersAndGetters() {
        PageQuery query = new PageQuery();
        query.setCurrent(2);
        query.setSize(20);
        query.setOrderBy("name");
        query.setOrderDirection("asc");

        assertEquals(2, query.getCurrent());
        assertEquals(20, query.getSize());
        assertEquals("name", query.getOrderBy());
        assertEquals("asc", query.getOrderDirection());
    }

    @Test
    void testIsAscWithDefaultDirection() {
        PageQuery query = new PageQuery();
        assertTrue(query.isAsc());
    }

    @Test
    void testIsAscWithAscending() {
        PageQuery query = new PageQuery();
        query.setOrderDirection("asc");
        assertTrue(query.isAsc());
    }

    @Test
    void testIsAscWithDescending() {
        PageQuery query = new PageQuery();
        query.setOrderDirection("desc");
        assertFalse(query.isAsc());
    }

    @Test
    void testIsAscCaseInsensitive() {
        PageQuery query = new PageQuery();
        query.setOrderDirection("DESC");
        assertFalse(query.isAsc());
    }

    @Test
    void testPageQueryImplementsSerializable() {
        PageQuery query = new PageQuery();
        assertInstanceOf(java.io.Serializable.class, query);
    }

    @Test
    void testPageQueryEqualsAndHashCode() {
        PageQuery query1 = new PageQuery();
        query1.setCurrent(1);
        query1.setSize(10);

        PageQuery query2 = new PageQuery();
        query2.setCurrent(1);
        query2.setSize(10);

        assertEquals(query1, query2);
        assertEquals(query1.hashCode(), query2.hashCode());
    }

    @Test
    void testPageQueryToString() {
        PageQuery query = new PageQuery();
        query.setCurrent(3);

        String toString = query.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("3"));
    }
}
