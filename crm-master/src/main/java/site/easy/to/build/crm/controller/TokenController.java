package site.easy.to.build.crm.controller;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

@RestController
public class TokenController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public TokenController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/api/token")
    public String getToken(@RequestParam("principal") String principal) {
        // Récupérer le client autorisé pour l'utilisateur
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", principal);

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            return accessToken.getTokenValue(); // Retourner le token d'accès
        }
        return "Token non disponible"; // Si aucun token n'est trouvé
    }
}
