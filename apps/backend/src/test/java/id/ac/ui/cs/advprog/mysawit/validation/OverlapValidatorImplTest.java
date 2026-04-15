package id.ac.ui.cs.advprog.mysawit.validation;

import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.repository.KebunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverlapValidatorImplTest {

    @Mock
    private KebunRepository kebunRepository;

    @InjectMocks
    private OverlapValidatorImpl overlapValidator;

    private GeometryFactory geometryFactory;
    private Polygon existingPolygon;
    private Kebun existingKebun;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();

        // Simulasi kebun di database: Kotak dari koordinat (0,0) ke (10,10)
        existingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(10, 10),
                new Coordinate(0, 10),
                new Coordinate(0, 0) // Menutup poligon
        });

        existingKebun = new Kebun();
        existingKebun.setId(1L);
        existingKebun.setNama("Kebun A");
        existingKebun.setWktGeometry(existingPolygon.toText());
    }

    @Test
    void testValidateNoOverlap_Success() {
        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(existingKebun));

        // Kebun baru berada jauh di luar area (20,20 ke 30,30)
        Polygon newPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(20, 20),
                new Coordinate(30, 20),
                new Coordinate(30, 30),
                new Coordinate(20, 30),
                new Coordinate(20, 20)
        });

        // Harusnya tidak melempar error
        assertDoesNotThrow(() -> overlapValidator.validateNoOverlap(newPolygon, null));
    }

    @Test
    void testValidateNoOverlap_ThrowsExceptionWhenOverlapping() {
        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(existingKebun));

        // Kebun baru menabrak kebun lama (5,5 ke 15,15)
        Polygon overlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(5, 5),
                new Coordinate(15, 5),
                new Coordinate(15, 15),
                new Coordinate(5, 15),
                new Coordinate(5, 5)
        });

        // Harusnya melempar IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            overlapValidator.validateNoOverlap(overlappingPolygon, null);
        });

        assertTrue(exception.getMessage().contains("Koordinat tumpang tindih dengan kebun: Kebun A"));
    }

    @Test
    void testValidateNoOverlap_SuccessWhenUpdatingSameKebun() {
        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(existingKebun));

        // Kebun menabrak lokasinya sendiri (saat proses Update / Edit data)
        Polygon overlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(5, 5),
                new Coordinate(15, 5),
                new Coordinate(15, 15),
                new Coordinate(5, 15),
                new Coordinate(5, 5)
        });

        // Karena ID yang di-exclude adalah 1L (dirinya sendiri), harusnya tidak error
        assertDoesNotThrow(() -> overlapValidator.validateNoOverlap(overlappingPolygon, 1L));
    }

    // ========== NEW EDGE CASE TESTS ==========

    @Test
    void testValidateNoOverlap_EmptyDatabase_NoError() {
        when(kebunRepository.findAll()).thenReturn(Collections.emptyList());

        Polygon newPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(10, 10),
                new Coordinate(0, 10),
                new Coordinate(0, 0)
        });

        assertDoesNotThrow(() -> overlapValidator.validateNoOverlap(newPolygon, null));
    }

    @Test
    void testValidateNoOverlap_InvalidWktInDatabase_ThrowsRuntimeException() {
        Kebun badKebun = new Kebun();
        badKebun.setId(2L);
        badKebun.setNama("Bad Kebun");
        badKebun.setWktGeometry("THIS IS NOT VALID WKT");

        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(badKebun));

        Polygon newPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(10, 10),
                new Coordinate(0, 10),
                new Coordinate(0, 0)
        });

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> overlapValidator.validateNoOverlap(newPolygon, null));
        assertTrue(ex.getMessage().contains("Gagal membaca data geometri"));
    }

    @Test
    void testValidateNoOverlap_MultipleKebun_OnlyOneOverlaps() {
        Kebun nonOverlappingKebun = new Kebun();
        nonOverlappingKebun.setId(2L);
        nonOverlappingKebun.setNama("Kebun Far Away");
        Polygon farPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(100, 100),
                new Coordinate(110, 100),
                new Coordinate(110, 110),
                new Coordinate(100, 110),
                new Coordinate(100, 100)
        });
        nonOverlappingKebun.setWktGeometry(farPolygon.toText());

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(nonOverlappingKebun, existingKebun));

        // This polygon overlaps with existingKebun (0,0 to 10,10)
        Polygon overlapping = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(5, 5),
                new Coordinate(15, 5),
                new Coordinate(15, 15),
                new Coordinate(5, 15),
                new Coordinate(5, 5)
        });

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> overlapValidator.validateNoOverlap(overlapping, null));
        assertTrue(ex.getMessage().contains("Kebun A"));
    }

    @Test
    void testValidateNoOverlap_MultipleKebun_NoneOverlap() {
        Kebun kebun2 = new Kebun();
        kebun2.setId(2L);
        kebun2.setNama("Kebun B");
        Polygon polygon2 = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(50, 50),
                new Coordinate(60, 50),
                new Coordinate(60, 60),
                new Coordinate(50, 60),
                new Coordinate(50, 50)
        });
        kebun2.setWktGeometry(polygon2.toText());

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(existingKebun, kebun2));

        // Far away from both
        Polygon newPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(200, 200),
                new Coordinate(210, 200),
                new Coordinate(210, 210),
                new Coordinate(200, 210),
                new Coordinate(200, 200)
        });

        assertDoesNotThrow(() -> overlapValidator.validateNoOverlap(newPolygon, null));
    }

    @Test
    void testValidateNoOverlap_AdjacentPolygons_NoOverlap() {
        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(existingKebun));

        // Adjacent polygon (shares a boundary edge but doesn't overlap interior)
        // Note: JTS `intersects()` returns true for shared boundaries (touches),
        // so this test verifies current behavior
        Polygon adjacent = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(10, 0),
                new Coordinate(20, 0),
                new Coordinate(20, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0)
        });

        // With current implementation using intersects(), touching polygons WILL trigger overlap
        // This documents the current behavior
        assertThrows(IllegalArgumentException.class,
                () -> overlapValidator.validateNoOverlap(adjacent, null));
    }

    @Test
    void testValidateNoOverlap_ExcludeIdNull_ChecksAllKebun() {
        when(kebunRepository.findAll()).thenReturn(Collections.singletonList(existingKebun));

        Polygon overlapping = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(5, 5),
                new Coordinate(15, 5),
                new Coordinate(15, 15),
                new Coordinate(5, 15),
                new Coordinate(5, 5)
        });

        // With null excludeId, all kebun are checked including existingKebun (id=1)
        assertThrows(IllegalArgumentException.class,
                () -> overlapValidator.validateNoOverlap(overlapping, null));
    }
}