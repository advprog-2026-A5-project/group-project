package id.ac.ui.cs.advprog.mysawit.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void commence_sendsUnauthorizedError() throws IOException {
        AuthenticationException authException = mock(AuthenticationException.class);

        authEntryPointJwt.commence(request, response, authException);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }

    @Test
    void commence_sends401StatusCode() throws IOException {
        AuthenticationException authException = mock(AuthenticationException.class);

        authEntryPointJwt.commence(request, response, authException);

        verify(response).sendError(eq(401), eq("Error: Unauthorized"));
    }
}
