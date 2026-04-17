package id.ac.ui.cs.advprog.mysawit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KebunTest {

    @Test
    void testGettersAndSetters() {
        Kebun kebun = new Kebun();
        kebun.setId(1L);
        kebun.setNama("Kebun Sawit A");
        kebun.setWktGeometry("POLYGON ((106 -6, 107 -6, 107 -7, 106 -7, 106 -6))");

        assertEquals(1L, kebun.getId());
        assertEquals("Kebun Sawit A", kebun.getNama());
        assertEquals("POLYGON ((106 -6, 107 -6, 107 -7, 106 -7, 106 -6))", kebun.getWktGeometry());
    }

    @Test
    void testDefaultValues() {
        Kebun kebun = new Kebun();
        assertNull(kebun.getId());
        assertNull(kebun.getNama());
        assertNull(kebun.getWktGeometry());
    }

    @Test
    void testSetNullValues() {
        Kebun kebun = new Kebun();
        kebun.setNama("Test");
        kebun.setNama(null);
        assertNull(kebun.getNama());
    }
}
