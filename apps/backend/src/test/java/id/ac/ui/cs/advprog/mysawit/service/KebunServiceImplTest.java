package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.CoordinateDTO;
import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.repository.KebunRepository;
import id.ac.ui.cs.advprog.mysawit.util.GeometryMapper;
import id.ac.ui.cs.advprog.mysawit.validation.OverlapValidator;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KebunServiceImplTest {

    @Mock
    private KebunRepository kebunRepository;

    @Mock
    private OverlapValidator overlapValidator;

    @Mock
    private GeometryMapper geometryMapper;

    @InjectMocks
    private KebunServiceImpl kebunService;

    private GeometryFactory geometryFactory;
    private Polygon mockPolygon;
    private KebunRequestDTO validRequest;
    private List<CoordinateDTO> validCoords;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();
        mockPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(10, 0),
                new Coordinate(10, 10),
                new Coordinate(0, 10),
                new Coordinate(0, 0)
        });

        validCoords = Arrays.asList(
                new CoordinateDTO(0.0, 0.0),
                new CoordinateDTO(0.0, 10.0),
                new CoordinateDTO(10.0, 10.0),
                new CoordinateDTO(10.0, 0.0)
        );

        validRequest = new KebunRequestDTO();
        validRequest.setNama("Kebun Sawit A");
        validRequest.setKodeKebun("KBN-001");
        validRequest.setLuas(10.0);
        validRequest.setKoordinat(validCoords);
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_validRequest_returnsKebun() {
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doNothing().when(overlapValidator).validateNoOverlap(mockPolygon, null);

        Kebun savedKebun = new Kebun();
        savedKebun.setId(1L);
        savedKebun.setNama("Kebun Sawit A");
        savedKebun.setWktGeometry(mockPolygon.toText());
        when(kebunRepository.save(any(Kebun.class))).thenReturn(savedKebun);

        Kebun result = kebunService.create(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Kebun Sawit A", result.getNama());
        verify(overlapValidator).validateNoOverlap(mockPolygon, null);
        verify(kebunRepository).save(any(Kebun.class));
    }

    @Test
    void create_overlappingCoordinates_throwsException() {
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doThrow(new IllegalArgumentException("Koordinat tumpang tindih dengan kebun: Kebun B"))
                .when(overlapValidator).validateNoOverlap(mockPolygon, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("tumpang tindih"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_invalidCoordinates_throwsException() {
        when(geometryMapper.createQuadrilateral(validCoords))
                .thenThrow(new IllegalArgumentException("Kebun harus memiliki tepat 4 titik koordinat."));

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_setsWktGeometry() {
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doNothing().when(overlapValidator).validateNoOverlap(mockPolygon, null);
        when(kebunRepository.save(any(Kebun.class))).thenAnswer(invocation -> {
            Kebun k = invocation.getArgument(0);
            k.setId(1L);
            return k;
        });

        Kebun result = kebunService.create(validRequest);

        assertEquals(mockPolygon.toText(), result.getWktGeometry());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_returnsAllKebun() {
        Kebun k1 = new Kebun();
        k1.setId(1L);
        k1.setNama("Kebun A");
        Kebun k2 = new Kebun();
        k2.setId(2L);
        k2.setNama("Kebun B");

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(k1, k2));

        List<Kebun> result = kebunService.findAll(null, null);

        assertEquals(2, result.size());
        verify(kebunRepository).findAll();
    }

    @Test
    void findAll_emptyDatabase_returnsEmptyList() {
        when(kebunRepository.findAll()).thenReturn(Collections.emptyList());

        List<Kebun> result = kebunService.findAll(null, null);

        assertTrue(result.isEmpty());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_existingId_returnsKebun() {
        Kebun kebun = new Kebun();
        kebun.setId(1L);
        kebun.setNama("Kebun A");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebun));

        Kebun result = kebunService.findById(1L);

        assertEquals("Kebun A", result.getNama());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.findById(99L));
        assertTrue(ex.getMessage().contains("Kebun tidak ditemukan"));
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_validRequest_returnsUpdatedKebun() {
        Kebun existing = new Kebun();
        existing.setId(1L);
        existing.setNama("Old Name");
        existing.setWktGeometry("POLYGON ((0 0, 5 0, 5 5, 0 5, 0 0))");

        when(kebunRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doNothing().when(overlapValidator).validateNoOverlap(mockPolygon, 1L);
        when(kebunRepository.save(any(Kebun.class))).thenReturn(existing);

        KebunRequestDTO updateRequest = new KebunRequestDTO();
        updateRequest.setNama("New Name");
        updateRequest.setKoordinat(validCoords);

        Kebun result = kebunService.update(1L, updateRequest);

        assertEquals("New Name", result.getNama());
        verify(overlapValidator).validateNoOverlap(mockPolygon, 1L);
        verify(kebunRepository).save(existing);
    }

    @Test
    void update_nonExistingKebun_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.update(99L, validRequest));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void update_overlappingWithOtherKebun_throwsException() {
        Kebun existing = new Kebun();
        existing.setId(1L);
        existing.setNama("Existing");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doThrow(new IllegalArgumentException("Koordinat tumpang tindih"))
                .when(overlapValidator).validateNoOverlap(mockPolygon, 1L);

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.update(1L, validRequest));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_existingKebun_deletesSuccessfully() {
        Kebun kebun = new Kebun();
        kebun.setId(1L);
        kebun.setNama("To Delete");
        kebun.setMandorName(null);
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebun));
        doNothing().when(kebunRepository).delete(kebun);

        assertDoesNotThrow(() -> kebunService.delete(1L));
        verify(kebunRepository).delete(kebun);
    }

    @Test
    void delete_nonExistingKebun_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.delete(99L));
        verify(kebunRepository, never()).delete(any(Kebun.class));
    }

    @Test
    void delete_kebunWithMandor_throwsException() {
        Kebun kebun = new Kebun();
        kebun.setId(1L);
        kebun.setNama("With Mandor");
        kebun.setMandorName("Budi");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(kebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.delete(1L));
        assertTrue(ex.getMessage().contains("masih terikat dengan seorang Mandor"));
        verify(kebunRepository, never()).delete(any(Kebun.class));
    }

    // ========== CREATE VALIDATION TESTS ==========

    @Test
    void create_nullKodeKebun_throwsException() {
        validRequest.setKodeKebun(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("Kode kebun wajib diisi"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_blankKodeKebun_throwsException() {
        validRequest.setKodeKebun("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("Kode kebun wajib diisi"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_nullLuas_throwsException() {
        validRequest.setLuas(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("Luas kebun wajib diisi"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_zeroLuas_throwsException() {
        validRequest.setLuas(0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("Luas kebun wajib diisi"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    @Test
    void create_negativeLuas_throwsException() {
        validRequest.setLuas(-5.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.create(validRequest));
        assertTrue(ex.getMessage().contains("Luas kebun wajib diisi"));
        verify(kebunRepository, never()).save(any(Kebun.class));
    }

    // ========== FIND ALL SEARCH FILTER TESTS ==========

    @Test
    void findAll_filterByNama_returnsMatchingKebun() {
        Kebun k1 = new Kebun();
        k1.setId(1L);
        k1.setNama("Kebun Durian");
        k1.setKodeKebun("KD-01");
        Kebun k2 = new Kebun();
        k2.setId(2L);
        k2.setNama("Kebun Sawit");
        k2.setKodeKebun("KS-01");

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(k1, k2));

        List<Kebun> result = kebunService.findAll("Durian", null);

        assertEquals(1, result.size());
        assertEquals("Kebun Durian", result.get(0).getNama());
    }

    @Test
    void findAll_filterByKode_returnsMatchingKebun() {
        Kebun k1 = new Kebun();
        k1.setId(1L);
        k1.setNama("Kebun A");
        k1.setKodeKebun("KBN-001");
        Kebun k2 = new Kebun();
        k2.setId(2L);
        k2.setNama("Kebun B");
        k2.setKodeKebun("KBN-002");

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(k1, k2));

        List<Kebun> result = kebunService.findAll(null, "002");

        assertEquals(1, result.size());
        assertEquals("KBN-002", result.get(0).getKodeKebun());
    }

    @Test
    void findAll_filterByBoth_returnsMatchingKebun() {
        Kebun k1 = new Kebun();
        k1.setId(1L);
        k1.setNama("Kebun Sawit Utara");
        k1.setKodeKebun("KSU-01");
        Kebun k2 = new Kebun();
        k2.setId(2L);
        k2.setNama("Kebun Sawit Selatan");
        k2.setKodeKebun("KSS-01");

        when(kebunRepository.findAll()).thenReturn(Arrays.asList(k1, k2));

        List<Kebun> result = kebunService.findAll("Utara", "KSU");

        assertEquals(1, result.size());
        assertEquals("Kebun Sawit Utara", result.get(0).getNama());
    }

    // ========== UPDATE KODE KEBUN IMMUTABILITY TEST ==========

    @Test
    void update_doesNotChangekodeKebun() {
        Kebun existing = new Kebun();
        existing.setId(1L);
        existing.setNama("Old Name");
        existing.setKodeKebun("ORIGINAL-CODE");
        existing.setLuas(10.0);
        existing.setWktGeometry("POLYGON ((0 0, 5 0, 5 5, 0 5, 0 0))");

        when(kebunRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geometryMapper.createQuadrilateral(validCoords)).thenReturn(mockPolygon);
        doNothing().when(overlapValidator).validateNoOverlap(mockPolygon, 1L);
        when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

        KebunRequestDTO updateRequest = new KebunRequestDTO();
        updateRequest.setNama("New Name");
        updateRequest.setKodeKebun("ATTEMPT-CHANGE");
        updateRequest.setLuas(20.0);
        updateRequest.setKoordinat(validCoords);

        Kebun result = kebunService.update(1L, updateRequest);

        // kodeKebun should remain ORIGINAL-CODE, not ATTEMPT-CHANGE
        assertEquals("ORIGINAL-CODE", result.getKodeKebun());
        assertEquals("New Name", result.getNama());
        assertEquals(20.0, result.getLuas());
    }
}
