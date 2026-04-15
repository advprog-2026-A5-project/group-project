package id.ac.ui.cs.advprog.mysawit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(1L, "admin", "secret123");
        assertEquals(1L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("secret123", user.getPassword());
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId(42L);
        user.setUsername("mandor_budi");
        user.setPassword("p@ssw0rd");

        assertEquals(42L, user.getId());
        assertEquals("mandor_budi", user.getUsername());
        assertEquals("p@ssw0rd", user.getPassword());
    }

    @Test
    void testSetNullUsername() {
        User user = new User(1L, "test", "pass");
        user.setUsername(null);
        assertNull(user.getUsername());
    }
}
