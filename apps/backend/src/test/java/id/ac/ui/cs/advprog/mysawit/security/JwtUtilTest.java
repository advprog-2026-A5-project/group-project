package id.ac.ui.cs.advprog.mysawit.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
                "thisIsATestSecretKeyThatIsLongEnoughForHmacSha256AlgorithmUsageInTests1234");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600000);
        jwtUtil.init();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("admin");

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("admin", username);
    }

    @Test
    void validateJwtToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("testuser");

        assertTrue(jwtUtil.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_malformedToken_returnsFalse() {
        assertFalse(jwtUtil.validateJwtToken("this.is.not.a.valid.jwt"));
    }

    @Test
    void validateJwtToken_emptyToken_returnsFalse() {
        assertFalse(jwtUtil.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_nullToken_returnsFalse() {
        assertFalse(jwtUtil.validateJwtToken(null));
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        // Create a JwtUtil with 0ms expiration
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "jwtSecret",
                "thisIsATestSecretKeyThatIsLongEnoughForHmacSha256AlgorithmUsageInTests1234");
        ReflectionTestUtils.setField(shortLivedJwtUtil, "jwtExpirationMs", 0);
        shortLivedJwtUtil.init();

        String token = shortLivedJwtUtil.generateToken("user");

        // Token should be expired immediately
        assertFalse(shortLivedJwtUtil.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_tokenSignedWithDifferentKey_returnsFalse() {
        // Generate token with one key
        String token = jwtUtil.generateToken("testuser");

        // Validate with a different key
        JwtUtil differentKeyUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentKeyUtil, "jwtSecret",
                "aDifferentSecretKeyThatIsAlsoLongEnoughForHmacSha256AlgorithmUsageHere12");
        ReflectionTestUtils.setField(differentKeyUtil, "jwtExpirationMs", 3600000);
        differentKeyUtil.init();

        // Token signed with a different key should fail validation
        // The jjwt library may throw SignatureException which extends SecurityException
        // validateJwtToken catches SecurityException, so it should return false
        boolean result = differentKeyUtil.validateJwtToken(token);
        assertFalse(result);
    }

    @Test
    void generateToken_differentUsernames_produceDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void validateJwtToken_randomGarbage_returnsFalse() {
        assertFalse(jwtUtil.validateJwtToken("aGFoYWhhLm5vdC5hLnRva2Vu"));
    }
}
