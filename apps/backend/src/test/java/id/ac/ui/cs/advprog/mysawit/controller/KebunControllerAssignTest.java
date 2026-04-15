package id.ac.ui.cs.advprog.mysawit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.dto.AssignRequestDTO;
import id.ac.ui.cs.advprog.mysawit.dto.ReassignRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.service.KebunService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KebunControllerAssignTest {

    @Mock
    private KebunService kebunService;

    @InjectMocks
    private KebunController kebunController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kebunController).build();
        objectMapper = new ObjectMapper();
    }

    private Kebun createKebun(Long id, String nama, String mandor) {
        Kebun kebun = new Kebun();
        kebun.setId(id);
        kebun.setNama(nama);
        kebun.setKodeKebun("KBN-" + id);
        kebun.setLuas(10.0);
        kebun.setWktGeometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
        kebun.setMandorName(mandor);
        return kebun;
    }

    // ========== PUT /api/kebun/{id}/assign-mandor ==========

    @Test
    void assignMandor_validRequest_returns200() throws Exception {
        AssignRequestDTO request = new AssignRequestDTO();
        request.setName("Budi");

        Kebun result = createKebun(1L, "Kebun A", "Budi");
        when(kebunService.assignMandor(eq(1L), eq("Budi"))).thenReturn(result);

        mockMvc.perform(put("/api/kebun/1/assign-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mandorName").value("Budi"));
    }

    @Test
    void assignMandor_invalidRequest_returns400() throws Exception {
        AssignRequestDTO request = new AssignRequestDTO();
        request.setName("");

        when(kebunService.assignMandor(eq(1L), eq("")))
                .thenThrow(new IllegalArgumentException("Nama mandor tidak boleh kosong"));

        mockMvc.perform(put("/api/kebun/1/assign-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== PUT /api/kebun/{id}/assign-supir ==========

    @Test
    void assignSupir_validRequest_returns200() throws Exception {
        AssignRequestDTO request = new AssignRequestDTO();
        request.setName("Ahmad");

        Kebun result = createKebun(1L, "Kebun A", null);
        result.getSupirNames().add("Ahmad");
        when(kebunService.assignSupir(eq(1L), eq("Ahmad"))).thenReturn(result);

        mockMvc.perform(put("/api/kebun/1/assign-supir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supirNames[0]").value("Ahmad"));
    }

    // ========== PUT /api/kebun/{id}/unassign-mandor (MOVE TO ANOTHER KEBUN) ==========

    @Test
    void unassignMandor_validTarget_returns200() throws Exception {
        ReassignRequestDTO request = new ReassignRequestDTO();
        request.setTargetKebunId(2L);

        Kebun result = createKebun(1L, "Kebun A", null); // mandor removed
        when(kebunService.unassignMandor(eq(1L), eq(2L))).thenReturn(result);

        mockMvc.perform(put("/api/kebun/1/unassign-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mandorName").isEmpty());
    }

    @Test
    void unassignMandor_targetAlreadyHasMandor_returns400() throws Exception {
        ReassignRequestDTO request = new ReassignRequestDTO();
        request.setTargetKebunId(2L);

        when(kebunService.unassignMandor(eq(1L), eq(2L)))
                .thenThrow(new IllegalArgumentException("Kebun tujuan sudah memiliki mandor"));

        mockMvc.perform(put("/api/kebun/1/unassign-mandor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== PUT /api/kebun/{id}/unassign-supir (MOVE TO ANOTHER KEBUN) ==========

    @Test
    void unassignSupir_validTarget_returns200() throws Exception {
        ReassignRequestDTO request = new ReassignRequestDTO();
        request.setSupirName("Ahmad");
        request.setTargetKebunId(2L);

        Kebun result = createKebun(1L, "Kebun A", null);
        when(kebunService.unassignSupir(eq(1L), eq("Ahmad"), eq(2L))).thenReturn(result);

        mockMvc.perform(put("/api/kebun/1/unassign-supir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void unassignSupir_invalidSupir_returns400() throws Exception {
        ReassignRequestDTO request = new ReassignRequestDTO();
        request.setSupirName("Ghost");
        request.setTargetKebunId(2L);

        when(kebunService.unassignSupir(eq(1L), eq("Ghost"), eq(2L)))
                .thenThrow(new IllegalArgumentException("Supir yang ingin dicopot tidak ditemukan"));

        mockMvc.perform(put("/api/kebun/1/unassign-supir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
