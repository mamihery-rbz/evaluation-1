package site.easy.to.build.crm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(@CookieValue(name = "JSESSIONID", required = false) String jsessionId) {
        if (jsessionId == null || !isValidSession(jsessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session invalide");
        }
        return ResponseEntity.ok("Session valide");
    }

    private boolean isValidSession(String jsessionId) {
        // Ici, tu peux vérifier dans ta base de données ou utiliser Spring Security
        return true; // Modifier selon ta logique
    }
}
