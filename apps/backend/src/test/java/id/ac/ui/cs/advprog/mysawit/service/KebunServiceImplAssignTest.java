package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.repository.KebunRepository;
import id.ac.ui.cs.advprog.mysawit.util.GeometryMapper;
import id.ac.ui.cs.advprog.mysawit.validation.OverlapValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KebunServiceImplAssignTest {

    @Mock
    private KebunRepository kebunRepository;

    @Mock
    private OverlapValidator overlapValidator;

    @Mock
    private GeometryMapper geometryMapper;

    @InjectMocks
    private KebunServiceImpl kebunService;

    private Kebun sourceKebun;
    private Kebun targetKebun;

    @BeforeEach
    void setUp() {
        sourceKebun = new Kebun();
        sourceKebun.setId(1L);
        sourceKebun.setNama("Kebun Sawit A");
        sourceKebun.setKodeKebun("KBN-A");
        sourceKebun.setLuas(10.0);
        sourceKebun.setWktGeometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");

        targetKebun = new Kebun();
        targetKebun.setId(2L);
        targetKebun.setNama("Kebun Sawit B");
        targetKebun.setKodeKebun("KBN-B");
        targetKebun.setLuas(15.0);
        targetKebun.setWktGeometry("POLYGON ((20 20, 30 20, 30 30, 20 30, 20 20))");
    }

    // ========== ASSIGN MANDOR TESTS ==========

    @Test
    void assignMandor_validName_setsMandor() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.save(any(Kebun.class))).thenReturn(sourceKebun);

        Kebun result = kebunService.assignMandor(1L, "Budi");

        assertEquals("Budi", result.getMandorName());
        verify(kebunRepository).save(sourceKebun);
    }

    @Test
    void assignMandor_nullName_throwsException() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignMandor(1L, null));
        assertTrue(ex.getMessage().contains("Nama mandor tidak boleh kosong"));
    }

    @Test
    void assignMandor_blankName_throwsException() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignMandor(1L, "   "));
        assertTrue(ex.getMessage().contains("Nama mandor tidak boleh kosong"));
    }

    @Test
    void assignMandor_nonExistingKebun_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignMandor(99L, "Budi"));
    }

    // ========== ASSIGN SUPIR TESTS ==========

    @Test
    void assignSupir_validName_addsSupir() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.save(any(Kebun.class))).thenReturn(sourceKebun);

        Kebun result = kebunService.assignSupir(1L, "Ahmad");

        assertTrue(result.getSupirNames().contains("Ahmad"));
        verify(kebunRepository).save(sourceKebun);
    }

    @Test
    void assignSupir_nullName_throwsException() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignSupir(1L, null));
    }

    @Test
    void assignSupir_blankName_throwsException() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignSupir(1L, ""));
    }

    @Test
    void assignSupir_duplicateName_throwsException() {
        sourceKebun.getSupirNames().add("Ahmad");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.assignSupir(1L, "Ahmad"));
        assertTrue(ex.getMessage().contains("sudah ditugaskan"));
    }

    // ========== UNASSIGN MANDOR (MOVE TO ANOTHER KEBUN) TESTS ==========

    @Test
    void unassignMandor_validTarget_movesMandorToTargetKebun() {
        sourceKebun.setMandorName("Budi");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(2L)).thenReturn(Optional.of(targetKebun));
        when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

        Kebun result = kebunService.unassignMandor(1L, 2L);

        assertNull(sourceKebun.getMandorName());
        assertEquals("Budi", targetKebun.getMandorName());
        verify(kebunRepository, times(2)).save(any(Kebun.class));
    }

    @Test
    void unassignMandor_noCurrentMandor_throwsException() {
        // mandorName is null — nothing to unassign
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignMandor(1L, 2L));
        assertTrue(ex.getMessage().contains("belum memiliki mandor"));
    }

    @Test
    void unassignMandor_targetAlreadyHasMandor_throwsException() {
        sourceKebun.setMandorName("Budi");
        targetKebun.setMandorName("Slamet");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(2L)).thenReturn(Optional.of(targetKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignMandor(1L, 2L));
        assertTrue(ex.getMessage().contains("sudah memiliki mandor"));
    }

    @Test
    void unassignMandor_sameSourceAndTarget_throwsException() {
        sourceKebun.setMandorName("Budi");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignMandor(1L, 1L));
        assertTrue(ex.getMessage().contains("harus berbeda"));
    }

    @Test
    void unassignMandor_sourceNotFound_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignMandor(99L, 2L));
    }

    @Test
    void unassignMandor_targetNotFound_throwsException() {
        sourceKebun.setMandorName("Budi");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignMandor(1L, 99L));
    }

    // ========== UNASSIGN SUPIR (MOVE TO ANOTHER KEBUN) TESTS ==========

    @Test
    void unassignSupir_validTarget_movesSupirToTargetKebun() {
        sourceKebun.getSupirNames().add("Ahmad");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(2L)).thenReturn(Optional.of(targetKebun));
        when(kebunRepository.save(any(Kebun.class))).thenAnswer(inv -> inv.getArgument(0));

        kebunService.unassignSupir(1L, "Ahmad", 2L);

        assertFalse(sourceKebun.getSupirNames().contains("Ahmad"));
        assertTrue(targetKebun.getSupirNames().contains("Ahmad"));
        verify(kebunRepository, times(2)).save(any(Kebun.class));
    }

    @Test
    void unassignSupir_supirNotInSource_throwsException() {
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignSupir(1L, "Ahmad", 2L));
        assertTrue(ex.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void unassignSupir_supirAlreadyInTarget_throwsException() {
        sourceKebun.getSupirNames().add("Ahmad");
        targetKebun.getSupirNames().add("Ahmad");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(2L)).thenReturn(Optional.of(targetKebun));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignSupir(1L, "Ahmad", 2L));
        assertTrue(ex.getMessage().contains("sudah bekerja di kebun tujuan"));
    }

    @Test
    void unassignSupir_sameSourceAndTarget_throwsException() {
        sourceKebun.getSupirNames().add("Ahmad");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignSupir(1L, "Ahmad", 1L));
        assertTrue(ex.getMessage().contains("harus berbeda"));
    }

    @Test
    void unassignSupir_sourceNotFound_throwsException() {
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignSupir(99L, "Ahmad", 2L));
    }

    @Test
    void unassignSupir_targetNotFound_throwsException() {
        sourceKebun.getSupirNames().add("Ahmad");
        when(kebunRepository.findById(1L)).thenReturn(Optional.of(sourceKebun));
        when(kebunRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kebunService.unassignSupir(1L, "Ahmad", 99L));
    }
}
