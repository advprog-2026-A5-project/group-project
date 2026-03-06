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
}