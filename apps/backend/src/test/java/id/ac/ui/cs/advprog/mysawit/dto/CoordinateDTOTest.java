package id.ac.ui.cs.advprog.mysawit.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateDTOTest {

    @Test
    void testNoArgsConstructor() {
        CoordinateDTO dto = new CoordinateDTO();
        assertEquals(0.0, dto.getLatitude());
        assertEquals(0.0, dto.getLongitude());
    }

    @Test
    void testAllArgsConstructor() {
        CoordinateDTO dto = new CoordinateDTO(-6.123, 106.456);
        assertEquals(-6.123, dto.getLatitude());
        assertEquals(106.456, dto.getLongitude());
    }

    @Test
    void testSetters() {
        CoordinateDTO dto = new CoordinateDTO();
        dto.setLatitude(-7.5);
        dto.setLongitude(110.3);
        assertEquals(-7.5, dto.getLatitude());
        assertEquals(110.3, dto.getLongitude());
    }

    @Test
    void testEqualsAndHashCode() {
        CoordinateDTO dto1 = new CoordinateDTO(-6.0, 106.0);
        CoordinateDTO dto2 = new CoordinateDTO(-6.0, 106.0);
        CoordinateDTO dto3 = new CoordinateDTO(-7.0, 107.0);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testToString() {
        CoordinateDTO dto = new CoordinateDTO(-6.0, 106.0);
        String str = dto.toString();
        assertTrue(str.contains("-6.0"));
        assertTrue(str.contains("106.0"));
    }

    @Test
    void testEqualsWithNull() {
        CoordinateDTO dto = new CoordinateDTO(1.0, 2.0);
        assertNotEquals(null, dto);
    }

    @Test
    void testEqualsWithDifferentType() {
        CoordinateDTO dto = new CoordinateDTO(1.0, 2.0);
        assertNotEquals("not a dto", dto);
    }

    @Test
    void testEqualsSameObject() {
        CoordinateDTO dto = new CoordinateDTO(1.0, 2.0);
        assertEquals(dto, dto);
    }
}
