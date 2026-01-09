package security;

import com.example.smartcity.security.CustomAccessDeniedHandler;
import com.example.smartcity.security.CustomAuthSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;

import java.io.IOException;

/**
 * Beans mockés pour les tests
 */
@TestConfiguration
public class MockHandlers {

    @Bean
    @Primary
    public CustomAuthSuccessHandler mockCustomAuthSuccessHandler() {
        return new CustomAuthSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    @Primary
    public CustomAccessDeniedHandler mockCustomAccessDeniedHandler() {
        return new CustomAccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request,
                               HttpServletResponse response,
                               org.springframework.security.access.AccessDeniedException accessDeniedException)
                    throws IOException, ServletException {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            }
        };
    }
}