package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/depenses")
public class DepenseAPIController {

    private final DepenseService depenseService;
    private final TicketService ticketService;
    private final LeadService leadService;

    public DepenseAPIController(DepenseService depenseService,
                                TicketService ticketService,
                                LeadService leadService) {
        this.depenseService = depenseService;
        this.ticketService = ticketService;
        this.leadService = leadService;
    }
    /*===================================TICKETS==========================================*/

    @GetMapping("/tickets")
    public ResponseEntity<List<Map<String, Object>>> getDepensesTickets() {
        List<Depense> allDepenses = depenseService.findAll();

        List<Map<String, Object>> depensesTickets = allDepenses.stream()
                .filter(depense -> depense.getTicket() != null)
                .map(this::convertDepenseToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(depensesTickets);
    }

    private Map<String, Object> convertDepenseToMap(Depense depense) {
        Map<String, Object> depenseMap = new HashMap<>();
        depenseMap.put("id", depense.getId());
        depenseMap.put("montant", depense.getMontant());
        depenseMap.put("created_at", depense.getCreated_at());

        if (depense.getTicket() != null) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("ticket_id", depense.getTicket().getTicketId());
            ticketMap.put("subject", depense.getTicket().getSubject());
            ticketMap.put("status", depense.getTicket().getStatus());
            ticketMap.put("priority", depense.getTicket().getPriority());

            depenseMap.put("ticket", ticketMap);
        }

        return depenseMap;
    }
    @GetMapping("/total/tickets")
    public ResponseEntity<BigDecimal> getTotalDepensesTickets() {
        BigDecimal total = depenseService.findAllWithTickets().stream()
                .map(Depense::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(total);
    }


    /*===================================LEAD==========================================*/
    @GetMapping("/leads")
    public ResponseEntity<List<Map<String, Object>>> getDepensesLeads() {
        List<Depense> allDepenses = depenseService.findAll();

        // Filtrer les dépenses avec lead non null et les transformer pour l'API
        List<Map<String, Object>> depensesLeads = allDepenses.stream()
                .filter(depense -> depense.getLead() != null)
                .map(this::convertDepenseLeadToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(depensesLeads);
    }

    private Map<String, Object> convertDepenseLeadToMap(Depense depense) {
        Map<String, Object> depenseMap = new HashMap<>();
        depenseMap.put("id", depense.getId());
        depenseMap.put("montant", depense.getMontant());
        depenseMap.put("created_at", depense.getCreated_at());

        // Ajouter les infos du lead associé
        if (depense.getLead() != null) {
            Map<String, Object> leadMap = new HashMap<>();
            leadMap.put("lead_id", depense.getLead().getLeadId());
            leadMap.put("name", depense.getLead().getName());
            leadMap.put("status", depense.getLead().getStatus());
            leadMap.put("phone", depense.getLead().getPhone());

            // Ajouter les infos du manager si présent
            if (depense.getLead().getManager() != null) {
                leadMap.put("manager_name", depense.getLead().getManager().getUsername());
            }

            // Ajouter les infos de l'employé si présent
            if (depense.getLead().getEmployee() != null) {
                leadMap.put("employee_name", depense.getLead().getEmployee().getUsername());
            }

            depenseMap.put("lead", leadMap);
        }

        return depenseMap;
    }

    @GetMapping("/total/leads")
    public ResponseEntity<BigDecimal> getTotalDepensesLeads() {
        BigDecimal total = depenseService.findAllWithLeads().stream()
                .map(Depense::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ResponseEntity.ok(total);
    }
    /*===================================IRAISANA==========================================*/

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepenseById(@PathVariable Long id) {
        Optional<Depense> depense = depenseService.findById(id);
        if (depense.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertDepenseToMap(depense.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepense(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<Depense> depenseOpt = depenseService.findById(id);
        if (depenseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Depense depense = depenseOpt.get();

        if (updates.containsKey("montant")) {
            depense.setMontant(new BigDecimal(updates.get("montant").toString()));
        }
        Depense updatedDepense = depenseService.save(depense);
        return ResponseEntity.ok(convertDepenseToMap(updatedDepense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepense(@PathVariable Long id) {
        Optional<Depense> depenseOpt = depenseService.findById(id);
        if (depenseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Depense depense = depenseOpt.get();

        // Supprimer le ticket associé s'il existe
        if (depense.getTicket() != null) {
            ticketService.delete(depense.getTicket());
        }

        // Supprimer le lead associé s'il existe
        if (depense.getLead() != null) {
            leadService.delete(depense.getLead());
        }

        // Enfin supprimer la dépense
        depenseService.delete(id);

        return ResponseEntity.ok().build();
    }

}