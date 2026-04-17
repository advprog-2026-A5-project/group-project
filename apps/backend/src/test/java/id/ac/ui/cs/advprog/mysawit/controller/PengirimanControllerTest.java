package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.enums.StatusPengiriman;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import id.ac.ui.cs.advprog.mysawit.model.User;
import id.ac.ui.cs.advprog.mysawit.service.PengirimanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PengirimanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PengirimanService pengirimanService;

    @InjectMocks
    private PengirimanController pengirimanController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pengirimanController).build();
    }

    @Test
    void testAssignDriverSuccess() throws Exception {
        Pengiriman pengiriman = Pengiriman.builder()
                .id(100L)
                .status(StatusPengiriman.MEMUAT)
                .totalWeightKg(120.0)
                .build();

        when(pengirimanService.assignDriver(eq(11L), any())).thenReturn(pengiriman);

        String requestBody = """
                {
                  "driverId": 2,
                  "harvestItems": [
                    {
                      "harvestId": "eb558e9f-1c39-460e-8860-71af6af63bd6",
                      "weightKg": 120.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pengiriman/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "MANDOR")
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(pengirimanService, times(1)).assignDriver(eq(11L), any());
    }

    @Test
    void testAssignDriverFailForbiddenRole() throws Exception {
        String requestBody = """
                {
                  "driverId": 2,
                  "harvestItems": [
                    {
                      "harvestId": "eb558e9f-1c39-460e-8860-71af6af63bd6",
                      "weightKg": 120.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pengiriman/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "DRIVER")
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only MANDOR can access this endpoint"));

        verifyNoInteractions(pengirimanService);
    }

    @Test
    void testAssignDriverFailBadRequest() throws Exception {
        when(pengirimanService.assignDriver(eq(11L), any()))
                .thenThrow(new IllegalArgumentException("Total weight exceeds limit"));

        String requestBody = """
                {
                  "driverId": 2,
                  "harvestItems": [
                    {
                      "harvestId": "eb558e9f-1c39-460e-8860-71af6af63bd6",
                      "weightKg": 450.0
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/pengiriman/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "MANDOR")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Total weight exceeds limit"));
    }

    @Test
    void testUpdateStatusSuccess() throws Exception {
        Pengiriman updated = Pengiriman.builder()
                .id(100L)
                .status(StatusPengiriman.MENGIRIM)
                .build();

        when(pengirimanService.updateStatusPengiriman(100L, 2L, StatusPengiriman.MENGIRIM))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/pengiriman/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "DRIVER")
                        .content("{\"newStatus\":\"MENGIRIM\"}"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1))
                .updateStatusPengiriman(100L, 2L, StatusPengiriman.MENGIRIM);
    }

    @Test
    void testUpdateStatusFailForbiddenRole() throws Exception {
        mockMvc.perform(patch("/api/pengiriman/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "MANDOR")
                        .content("{\"newStatus\":\"MENGIRIM\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only DRIVER can access this endpoint"));

        verifyNoInteractions(pengirimanService);
    }

    @Test
    void testGetPengirimanByDriverForDriverSuccess() throws Exception {
        when(pengirimanService.getPengirimanByDriver(2L)).thenReturn(List.of(
                Pengiriman.builder().id(101L).status(StatusPengiriman.MEMUAT).build()
        ));

        mockMvc.perform(get("/api/pengiriman/driver/2")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).getPengirimanByDriver(2L);
    }

    @Test
    void testGetPengirimanByDriverForDriverFailForbiddenDifferentDriver() throws Exception {
        mockMvc.perform(get("/api/pengiriman/driver/2")
                        .header("X-User-Id", "3")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Driver can only access their own shipments"));

        verifyNoInteractions(pengirimanService);
    }

    @Test
    void testGetPengirimanByDriverForMandorSuccess() throws Exception {
        when(pengirimanService.getPengirimanByDriverForMandor(11L, 2L)).thenReturn(List.of(
                Pengiriman.builder().id(102L).status(StatusPengiriman.MENGIRIM).build()
        ));

        mockMvc.perform(get("/api/pengiriman/driver/2")
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).getPengirimanByDriverForMandor(11L, 2L);
    }

    @Test
    void testGetDriversSuccess() throws Exception {
        when(pengirimanService.getAvailableDriversForMandor(11L, "ri")).thenReturn(List.of(
                new User(2L, "driver.rifky", "secret")
        ));

        mockMvc.perform(get("/api/pengiriman/drivers")
                        .param("searchName", "ri")
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).getAvailableDriversForMandor(11L, "ri");
    }

        @Test
        void testGetPengirimanHistoryByDriverSuccess() throws Exception {
                LocalDate startDate = LocalDate.of(2026, 4, 1);
                LocalDate endDate = LocalDate.of(2026, 4, 30);

                when(pengirimanService.getPengirimanHistoryByDriver(2L, startDate, endDate)).thenReturn(List.of(
                                Pengiriman.builder().id(201L).status(StatusPengiriman.REJECTED_MANDOR).build()
                ));

                mockMvc.perform(get("/api/pengiriman/driver/2/history")
                                                .param("startDate", "2026-04-01")
                                                .param("endDate", "2026-04-30")
                                                .header("X-User-Id", "2")
                                                .header("X-User-Role", "DRIVER"))
                                .andExpect(status().isOk());

                verify(pengirimanService, times(1)).getPengirimanHistoryByDriver(2L, startDate, endDate);
        }

        @Test
        void testGetPengirimanHistoryByDriverFailForbidden() throws Exception {
                mockMvc.perform(get("/api/pengiriman/driver/2/history")
                                                .header("X-User-Id", "99")
                                                .header("X-User-Role", "DRIVER"))
                                .andExpect(status().isForbidden())
                                .andExpect(content().string("Driver can only access their own shipment history"));

                verify(pengirimanService, never()).getPengirimanHistoryByDriver(any(), any(), any());
        }

    @Test
    void testGetOngoingPengirimanFailForbidden() throws Exception {
        mockMvc.perform(get("/api/pengiriman/ongoing")
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isForbidden());

        verify(pengirimanService, never()).getOngoingPengiriman(any());
    }

    @Test
    void testApproveByMandorSuccess() throws Exception {
        Pengiriman approved = Pengiriman.builder()
                .id(100L)
                .status(StatusPengiriman.APPROVED_MANDOR)
                .build();

        when(pengirimanService.approveByMandor(100L, 11L)).thenReturn(approved);

        mockMvc.perform(patch("/api/pengiriman/100/mandor/approve")
                        .header("X-User-Id", "11")
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).approveByMandor(100L, 11L);
    }

    @Test
    void testRejectByMandorFailForbiddenRole() throws Exception {
        mockMvc.perform(patch("/api/pengiriman/100/mandor/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rejectionReason\":\"Muatan tidak sesuai\"}")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only MANDOR can access this endpoint"));

        verify(pengirimanService, never()).rejectByMandor(any(), any(), any());
    }

    @Test
    void testGetApprovedByMandorForAdminSuccess() throws Exception {
        LocalDate date = LocalDate.of(2026, 4, 17);
        when(pengirimanService.getApprovedPengirimanForAdmin("man", date)).thenReturn(List.of(
                Pengiriman.builder().id(300L).status(StatusPengiriman.APPROVED_MANDOR).build()
        ));

        mockMvc.perform(get("/api/pengiriman/admin/approved-mandor")
                        .param("mandorName", "man")
                        .param("date", "2026-04-17")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).getApprovedPengirimanForAdmin("man", date);
    }

    @Test
    void testApproveByAdminSuccess() throws Exception {
        Pengiriman approved = Pengiriman.builder()
                .id(400L)
                .status(StatusPengiriman.APPROVED_ADMIN)
                .build();

        when(pengirimanService.approveByAdmin(400L, 90L)).thenReturn(approved);

        mockMvc.perform(patch("/api/pengiriman/400/admin/approve")
                        .header("X-User-Id", "90")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(pengirimanService, times(1)).approveByAdmin(400L, 90L);
    }

    @Test
    void testPartialRejectByAdminBadRequest() throws Exception {
        when(pengirimanService.partialRejectByAdmin(500L, 90L, 0.0, "Sebagian rusak"))
                .thenThrow(new IllegalArgumentException("Acknowledged weight must be greater than 0"));

        mockMvc.perform(patch("/api/pengiriman/500/admin/partial-reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"acknowledgedWeightKg\":0.0,\"rejectionReason\":\"Sebagian rusak\"}")
                        .header("X-User-Id", "90")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Acknowledged weight must be greater than 0"));
    }

    @Test
    void testGetPengirimanByIdNotFound() throws Exception {
        when(pengirimanService.getPengirimanById(999L))
                .thenThrow(new IllegalArgumentException("Pengiriman not found"));

        mockMvc.perform(get("/api/pengiriman/999"))
                .andExpect(status().isNotFound());
    }
}
