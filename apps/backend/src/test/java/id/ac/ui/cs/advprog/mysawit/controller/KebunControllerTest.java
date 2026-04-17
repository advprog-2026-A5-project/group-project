package id.ac.ui.cs.advprog.mysawit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.dto.CoordinateDTO;
import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KebunControllerTest {

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

    private KebunRequestDTO createValidRequest() {
        KebunRequestDTO request = new KebunRequestDTO();
        request.setNama("Kebun Baru");
        request.setKoordinat(Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0)
        ));
        return request;
    }

    private Kebun createKebun(Long id, String nama) {
        Kebun kebun = new Kebun();
        kebun.setId(id);
        kebun.setNama(nama);
        kebun.setWktGeometry("POLYGON ((106 -6, 107 -6, 107 -7, 106 -7, 106 -6))");
        return kebun;
    }

    // ========== POST /api/kebun ==========

    @Test
    void create_validRequest_returns200() throws Exception {
        KebunRequestDTO request = createValidRequest();
        Kebun saved = createKebun(1L, "Kebun Baru");

        when(kebunService.create(any(KebunRequestDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/kebun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nama").value("Kebun Baru"));
    }

    @Test
    void create_invalidRequest_returns400() throws Exception {
        KebunRequestDTO request = createValidRequest();
        when(kebunService.create(any(KebunRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Kebun harus memiliki tepat 4 titik koordinat."));

        mockMvc.perform(post("/api/kebun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("4 titik koordinat")));
    }

    @Test
    void create_overlappingCoordinates_returns400() throws Exception {
        KebunRequestDTO request = createValidRequest();
        when(kebunService.create(any(KebunRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Koordinat tumpang tindih dengan kebun: Kebun A"));

        mockMvc.perform(post("/api/kebun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("tumpang tindih")));
    }

    // ========== GET /api/kebun ==========

    @Test
    void getAll_returnsListOfKebun() throws Exception {
        List<Kebun> kebunList = Arrays.asList(
                createKebun(1L, "Kebun A"),
                createKebun(2L, "Kebun B")
        );
        when(kebunService.findAll(isNull(), isNull())).thenReturn(kebunList);

        mockMvc.perform(get("/api/kebun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nama").value("Kebun A"))
                .andExpect(jsonPath("$[1].nama").value("Kebun B"));
    }

    @Test
    void getAll_emptyList_returns200() throws Exception {
        when(kebunService.findAll(isNull(), isNull())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/kebun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========== GET /api/kebun/{id} ==========

    @Test
    void getById_existingKebun_returns200() throws Exception {
        Kebun kebun = createKebun(1L, "Kebun A");
        when(kebunService.findById(1L)).thenReturn(kebun);

        mockMvc.perform(get("/api/kebun/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nama").value("Kebun A"));
    }

    @Test
    void getById_nonExistingKebun_returns404() throws Exception {
        when(kebunService.findById(99L))
                .thenThrow(new IllegalArgumentException("Kebun tidak ditemukan"));

        mockMvc.perform(get("/api/kebun/99"))
                .andExpect(status().isNotFound());
    }

    // ========== PUT /api/kebun/{id} ==========

    @Test
    void update_validRequest_returns200() throws Exception {
        KebunRequestDTO request = createValidRequest();
        Kebun updated = createKebun(1L, "Kebun Updated");

        when(kebunService.update(eq(1L), any(KebunRequestDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/kebun/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value("Kebun Updated"));
    }

    @Test
    void update_invalidRequest_returns400() throws Exception {
        KebunRequestDTO request = createValidRequest();
        when(kebunService.update(eq(1L), any(KebunRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Koordinat tumpang tindih"));

        mockMvc.perform(put("/api/kebun/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== DELETE /api/kebun/{id} ==========

    @Test
    void delete_existingKebun_returns200() throws Exception {
        doNothing().when(kebunService).delete(1L);

        mockMvc.perform(delete("/api/kebun/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("berhasil dihapus")));
    }

    @Test
    void delete_nonExistingKebun_returns404() throws Exception {
        doThrow(new IllegalArgumentException("Kebun tidak ditemukan"))
                .when(kebunService).delete(99L);

        mockMvc.perform(delete("/api/kebun/99"))
                .andExpect(status().isBadRequest());
    }
}
