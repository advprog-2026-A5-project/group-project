package id.ac.ui.cs.advprog.mysawit.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KebunRequestDTOTest {

    @Test
    void testDefaultValues() {
        KebunRequestDTO dto = new KebunRequestDTO();
        assertNull(dto.getNama());
        assertNull(dto.getKoordinat());
    }

    @Test
    void testSettersAndGetters() {
        KebunRequestDTO dto = new KebunRequestDTO();
        dto.setNama("Kebun Baru");

        List<CoordinateDTO> coords = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0)
        );
        dto.setKoordinat(coords);

        assertEquals("Kebun Baru", dto.getNama());
        assertEquals(4, dto.getKoordinat().size());
        assertEquals(-6.0, dto.getKoordinat().get(0).getLatitude());
    }

    @Test
    void testEqualsAndHashCode() {
        KebunRequestDTO dto1 = new KebunRequestDTO();
        dto1.setNama("A");
        dto1.setKoordinat(Collections.emptyList());

        KebunRequestDTO dto2 = new KebunRequestDTO();
        dto2.setNama("A");
        dto2.setKoordinat(Collections.emptyList());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        KebunRequestDTO dto1 = new KebunRequestDTO();
        dto1.setNama("A");

        KebunRequestDTO dto2 = new KebunRequestDTO();
        dto2.setNama("B");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        KebunRequestDTO dto = new KebunRequestDTO();
        dto.setNama("Test");
        String str = dto.toString();
        assertTrue(str.contains("Test"));
    }
}
