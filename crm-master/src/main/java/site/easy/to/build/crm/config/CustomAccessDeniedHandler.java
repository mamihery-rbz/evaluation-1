package site.easy.to.build.crm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        System.out.println("⛔️ Accès refusé pour l'URL: " + request.getRequestURI());
        System.out.println("📢 Raison: " + accessDeniedException.getMessage());
        response.sendRedirect("/access-denied");
    }
}
