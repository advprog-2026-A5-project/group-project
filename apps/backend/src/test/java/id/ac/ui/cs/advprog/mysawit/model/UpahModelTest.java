package id.ac.ui.cs.advprog.mysawit.model;

import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import id.ac.ui.cs.advprog.mysawit.repository.UpahRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UpahModelTest {

    @Autowired
    private UpahRepository upahRepository;

    @BeforeEach
    void setUp() {
        upahRepository.deleteAll();
    }

    @Test
    void saveAndLoadByRole() {
        Upah upah = new Upah();
        upah.setRole(UpahRole.BURUH);
        upah.setUpahPerKg(1000.0);

        upahRepository.save(upah);

        Upah loaded = upahRepository.findByRole(UpahRole.BURUH).orElseThrow();
        assertEquals(UpahRole.BURUH, loaded.getRole());
        assertEquals(1000.0, loaded.getUpahPerKg());
    }
}
