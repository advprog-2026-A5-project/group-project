package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.UpahRequestDTO;
import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.repository.UpahRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UpahServiceImplTest {

    @Autowired
    private UpahServiceImpl upahService;

    @Autowired
    private UpahRepository upahRepository;

    @BeforeEach
    void setUp() {
        upahRepository.deleteAll();
        upahService.ensureRolesExist();
    }

    @Test
    void ensureRolesExist_createsThreeRecords() {
        List<Upah> all = upahRepository.findAll();
        assertEquals(3, all.size());
        assertNotNull(upahRepository.findByRole(UpahRole.BURUH).orElse(null));
        assertNotNull(upahRepository.findByRole(UpahRole.MANDOR).orElse(null));
        assertNotNull(upahRepository.findByRole(UpahRole.SUPIR).orElse(null));
    }

    @Test
    void update_updatesRoleValue() {
        UpahRequestDTO request = new UpahRequestDTO();
        request.setRole(UpahRole.MANDOR);
        request.setUpahPerKg(1500.0);

        Upah updated = upahService.update(request);

        assertEquals(UpahRole.MANDOR, updated.getRole());
        assertEquals(1500.0, updated.getUpahPerKg());
    }

    @Test
    void update_rejectsMissingRole() {
        UpahRequestDTO request = new UpahRequestDTO();
        request.setUpahPerKg(1000.0);

        assertThrows(IllegalArgumentException.class, () -> upahService.update(request));
    }

    @Test
    void update_rejectsMissingValue() {
        UpahRequestDTO request = new UpahRequestDTO();
        request.setRole(UpahRole.BURUH);

        assertThrows(IllegalArgumentException.class, () -> upahService.update(request));
    }

    @Test
    void update_rejectsNonPositiveValue() {
        UpahRequestDTO request = new UpahRequestDTO();
        request.setRole(UpahRole.SUPIR);
        request.setUpahPerKg(0.0);

        assertThrows(IllegalArgumentException.class, () -> upahService.update(request));
    }
}
