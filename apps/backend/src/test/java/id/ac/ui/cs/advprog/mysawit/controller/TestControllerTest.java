package id.ac.ui.cs.advprog.mysawit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    @InjectMocks
    private TestController testController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
    }

    @Test
    void allAccess_returnsPublicContent() throws Exception {
        mockMvc.perform(get("/api/test/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public Content."));
    }

    @Test
    void userAccess_returnsUserContent() throws Exception {
        mockMvc.perform(get("/api/test/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Content."));
    }
}
