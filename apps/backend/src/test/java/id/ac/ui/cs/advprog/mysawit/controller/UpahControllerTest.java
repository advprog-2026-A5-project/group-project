package id.ac.ui.cs.advprog.mysawit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawit.dto.UpahRequestDTO;
import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.service.UpahService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UpahControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpahService upahService;

    @Test
    void get_returnsListWhenAdmin() throws Exception {
        Upah buruh = new Upah(1L, UpahRole.BURUH, 1000.0);
        Upah mandor = new Upah(2L, UpahRole.MANDOR, 1500.0);

        when(upahService.getAll()).thenReturn(List.of(buruh, mandor));

        mockMvc.perform(get("/api/upah")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("BURUH"))
                .andExpect(jsonPath("$[0].upahPerKg").value(1000.0))
                .andExpect(jsonPath("$[1].role").value("MANDOR"))
                .andExpect(jsonPath("$[1].upahPerKg").value(1500.0));
    }

    @Test
    void get_forbiddenWhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/upah")
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_updatesRoleValueWhenAdmin() throws Exception {
        UpahRequestDTO request = new UpahRequestDTO();
        request.setRole(UpahRole.SUPIR);
        request.setUpahPerKg(1200.0);

        Upah updated = new Upah(3L, UpahRole.SUPIR, 1200.0);
        when(upahService.update(request)).thenReturn(updated);

        mockMvc.perform(put("/api/upah")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPIR"))
                .andExpect(jsonPath("$.upahPerKg").value(1200.0));
    }
}
