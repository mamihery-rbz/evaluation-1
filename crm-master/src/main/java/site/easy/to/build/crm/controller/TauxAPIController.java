package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Taux;
import site.easy.to.build.crm.repository.TauxRepository;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/taux")
public class TauxAPIController {

    @Autowired
    private TauxRepository tauxRepository;

    @GetMapping("/latest")
    public ResponseEntity<Taux> getLatestTaux() {
        Taux latestTaux = tauxRepository.findTopByOrderByCreatedAtDesc().orElse(null);
        return latestTaux != null ? ResponseEntity.ok(latestTaux) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping
    public ResponseEntity<?> addTaux(@RequestBody Map<String, String> request) {
        try {
            BigDecimal valeur = new BigDecimal(request.get("valeur"));
            Taux newTaux = new Taux();
            newTaux.setValeur(valeur);
            tauxRepository.save(newTaux);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La valeur doit être un nombre valide.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur s'est produite lors de l'ajout du taux.");
        }
    }
}