package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.AssignDriverRequest;
import id.ac.ui.cs.advprog.mysawit.enums.StatusPengiriman;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import id.ac.ui.cs.advprog.mysawit.model.User;
import id.ac.ui.cs.advprog.mysawit.repository.PengirimanRepository;
import id.ac.ui.cs.advprog.mysawit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PengirimanServiceImplTest {

    @Mock
    private PengirimanRepository pengirimanRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PengirimanServiceImpl pengirimanService;

    @Test
    void assignDriver_shouldRejectEmptyHarvestItems() {
        AssignDriverRequest request = AssignDriverRequest.builder()
                .driverId(2L)
                .harvestItems(List.of())
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.assignDriver(1L, request)
        );

        assertTrue(ex.getMessage().contains("At least one harvest item is required"));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(pengirimanRepository);
    }

    @Test
    void assignDriver_shouldRejectDuplicateHarvestItemInRequest() {
        UUID duplicateHarvestId = UUID.randomUUID();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "mandor", "secret")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "driver", "secret")));
        when(pengirimanRepository.countActiveShipmentByHarvestId(eq(duplicateHarvestId), anyCollection()))
                .thenReturn(0L);

        AssignDriverRequest request = AssignDriverRequest.builder()
                .driverId(2L)
                .harvestItems(List.of(
                        new AssignDriverRequest.HarvestItemDto(duplicateHarvestId, 100.0),
                        new AssignDriverRequest.HarvestItemDto(duplicateHarvestId, 50.0)
                ))
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.assignDriver(1L, request)
        );

        assertTrue(ex.getMessage().contains("Duplicate harvest item"));
    }

    @Test
    void assignDriver_shouldRejectAlreadyAssignedHarvestItem() {
        UUID harvestId = UUID.randomUUID();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "mandor", "secret")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "driver", "secret")));
        when(pengirimanRepository.countActiveShipmentByHarvestId(eq(harvestId), anyCollection()))
                .thenReturn(1L);

        AssignDriverRequest request = AssignDriverRequest.builder()
                .driverId(2L)
                .harvestItems(List.of(new AssignDriverRequest.HarvestItemDto(harvestId, 120.0)))
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.assignDriver(1L, request)
        );

        assertTrue(ex.getMessage().contains("already assigned to an active shipment"));
    }

    @Test
    void getPengirimanByDriver_shouldUseActiveStatusesOnly() {
        List<Pengiriman> expected = List.of(Pengiriman.builder().id(1L).build());
        when(pengirimanRepository.findByDriverIdAndStatusIn(eq(2L), anyCollection())).thenReturn(expected);

        List<Pengiriman> result = pengirimanService.getPengirimanByDriver(2L);

        assertEquals(expected, result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<StatusPengiriman>> statusesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(pengirimanRepository).findByDriverIdAndStatusIn(eq(2L), statusesCaptor.capture());

        Collection<StatusPengiriman> statuses = statusesCaptor.getValue();
        assertTrue(statuses.contains(StatusPengiriman.MEMUAT));
        assertTrue(statuses.contains(StatusPengiriman.MENGIRIM));
        assertTrue(statuses.contains(StatusPengiriman.TIBA_DI_TUJUAN));
    }

    @Test
    void getAvailableDriversForMandor_shouldFilterByNameAndExcludeMandorSelf() {
        User mandor = new User(1L, "mandor.satu", "secret");
        User driverMatch = new User(2L, "driver.rifky", "secret");
        User sameMandorFromResult = new User(1L, "mandor.satu", "secret");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mandor));
        when(userRepository.findByUsernameContainingIgnoreCase("ri"))
                .thenReturn(List.of(driverMatch, sameMandorFromResult));

        List<User> result = pengirimanService.getAvailableDriversForMandor(1L, "ri");

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        assertEquals("driver.rifky", result.getFirst().getUsername());
    }

    @Test
    void approveByMandor_shouldSetApprovedStatusAndAcknowledgedWeight() {
        User mandor = new User(1L, "mandor.satu", "secret");
        Pengiriman pengiriman = Pengiriman.builder()
                .id(10L)
                .mandor(mandor)
                .status(StatusPengiriman.TIBA_DI_TUJUAN)
                .totalWeightKg(250.0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mandor));
        when(pengirimanRepository.findById(10L)).thenReturn(Optional.of(pengiriman));
        when(pengirimanRepository.save(any(Pengiriman.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pengiriman result = pengirimanService.approveByMandor(10L, 1L);

        assertEquals(StatusPengiriman.APPROVED_MANDOR, result.getStatus());
        assertEquals(250.0, result.getAcknowledgedWeightKg());
        assertNull(result.getRejectionReason());
    }

    @Test
    void rejectByMandor_shouldRequireReason() {
        User mandor = new User(1L, "mandor.satu", "secret");
        Pengiriman pengiriman = Pengiriman.builder()
                .id(10L)
                .mandor(mandor)
                .status(StatusPengiriman.TIBA_DI_TUJUAN)
                .totalWeightKg(250.0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mandor));
        when(pengirimanRepository.findById(10L)).thenReturn(Optional.of(pengiriman));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.rejectByMandor(10L, 1L, "  ")
        );

        assertEquals("Rejection reason is required", ex.getMessage());
        verify(pengirimanRepository, never()).save(any());
    }

    @Test
    void getPengirimanHistoryByDriver_shouldRejectInvalidDateRange() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "driver", "secret")));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.getPengirimanHistoryByDriver(
                        2L,
                        LocalDate.of(2026, 4, 20),
                        LocalDate.of(2026, 4, 19)
                )
        );

        assertEquals("End date must be after or equal to start date", ex.getMessage());
        verify(pengirimanRepository, never()).findDriverHistory(any(), any(), any(), any());
    }

    @Test
    void partialRejectByAdmin_shouldRejectWhenAcknowledgedWeightIsNotPartial() {
        User admin = new User(99L, "admin", "secret");
        Pengiriman pengiriman = Pengiriman.builder()
                .id(20L)
                .status(StatusPengiriman.APPROVED_MANDOR)
                .totalWeightKg(300.0)
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));
        when(pengirimanRepository.findById(20L)).thenReturn(Optional.of(pengiriman));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pengirimanService.partialRejectByAdmin(20L, 99L, 300.0, "Sebagian rusak")
        );

        assertEquals("Acknowledged weight for partial rejection must be less than total shipment weight", ex.getMessage());
        verify(pengirimanRepository, never()).save(any());
    }
}
