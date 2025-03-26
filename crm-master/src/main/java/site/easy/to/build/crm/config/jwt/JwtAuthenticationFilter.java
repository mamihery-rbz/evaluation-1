package site.easy.to.build.crm.config.jwt;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String decoded = new String(Base64.getDecoder().decode(token));
                String[] parts = decoded.split(":");

                if (parts.length == 2) {
                    String username = parts[0];
                    long timestamp = Long.parseLong(parts[1]);

                    // Vérifiez si le token a expiré (ex: 24h)
                    if (System.currentTimeMillis() - timestamp > 86400000) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                        return;
                    }

                    List<User> users = userService.findByUsername(username);
                    if (!users.isEmpty()) {
                        User user = users.get(0);
                        UsernamePasswordAuthenticationToken authentication = createAuthentication(user);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
    }
}
