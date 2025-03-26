package site.easy.to.build.crm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.MontantLead;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.service.lead.MontantLeadService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leads")
public class LeadAPIController {
    private final LeadRepository leadRepository;
    private final MontantLeadService montantLeadService;


    public LeadAPIController(LeadRepository leadRepository, MontantLeadService montantLeadService) {
        this.leadRepository = leadRepository;
        this.montantLeadService = montantLeadService;

    }

    @GetMapping("/total")
    public int getTotalLeads() {
        return leadRepository.findAll().size();
    }


    @GetMapping("/by-status")
    public Map<String, Integer> getLeadsByStatus() {
        List<Lead> leads = leadRepository.findAll();
        Map<String, Integer> leadsByStatus = new HashMap<>();

        // Initialiser les statuts possibles
        String[] statuses = {
                "Meeting to be scheduled", "Assign to sales representative","Archived","success"
        };

        for (String status : statuses) {
            leadsByStatus.put(status, 0);
        }

        // Compter les leads par statut
        for (Lead lead : leads) {
            String status = lead.getStatus();
            leadsByStatus.put(status, leadsByStatus.getOrDefault(status, 0) + 1);
        }

        return leadsByStatus;
    }

    @GetMapping("/by-year")
    public Map<String, Integer> getLeadsByYear(@RequestParam int year) {
        List<Lead> leads = leadRepository.findAll();
        Map<String, Integer> leadsByMonth = new HashMap<>();

        // Liste ordonnée des mois
        String[] orderedMonths = {
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        };

        // Initialiser la Map avec les mois dans l'ordre
        for (String month : orderedMonths) {
            leadsByMonth.put(month, 0);
        }

        // Compter les leads par mois
        for (Lead lead : leads) {
            LocalDateTime createdAt = lead.getCreatedAt();
            if (createdAt != null && createdAt.getYear() == year) {
                String month = createdAt.getMonth().toString();
                leadsByMonth.put(month, leadsByMonth.getOrDefault(month, 0) + 1);
            }
        }

        return leadsByMonth;
    }

    @GetMapping("/all")
    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable int id) {
        Optional<Lead> lead = leadRepository.findById(id);
        if (lead.isPresent()) {
            leadRepository.delete(lead.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lead non trouvé");
    }

    @GetMapping("/{id}/montant")
    public ResponseEntity<MontantLead> getMontantTicket(@PathVariable int id) {
        Optional<Lead> lead = leadRepository.findById(id);
        if (lead.isPresent()) {
            MontantLead montantLead = montantLeadService.findByLead(lead.get()).stream().findFirst().orElse(null);
            if (montantLead != null) {
                return ResponseEntity.ok(montantLead);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("/{id}/montant")
    public ResponseEntity<?> updateMontantTicket(@PathVariable int id, @RequestBody Map<String, String> request) {
        Optional<Lead> lead = leadRepository.findById(id);
        if (lead.isPresent()) {
            MontantLead montantLead = montantLeadService.findByLead(lead.get()).stream().findFirst().orElse(null);
            if (montantLead != null) {
                montantLead.setMontant(new BigDecimal(request.get("montant")));
                montantLeadService.save(montantLead);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lead non trouvé");
    }
}