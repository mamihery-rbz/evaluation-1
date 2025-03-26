package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.MontantTicket;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.ticket.MontantTicketService;
import site.easy.to.build.crm.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketAPIController {
    @Autowired
    private TicketService ticketService;

    private final TicketRepository ticketRepository;
    private final MontantTicketService montantTicketService;

    public TicketAPIController(TicketRepository ticketRepository, MontantTicketService montantTicketService) {
        this.ticketRepository = ticketRepository;
        this.montantTicketService = montantTicketService;
    }

    @GetMapping("/total")
    public int getTotalTickets() {
        return ticketRepository.findAll().size();
    }

    @GetMapping("/by-status")
    public Map<String, Integer> getTicketsByStatus() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Integer> ticketsByStatus = new HashMap<>();

        String[] statuses = {
                "Open", "assigned", "On Hold", "In Progress", "Resolved", "Closed", "Reopened", "Pending Customer Response", "Escalated", "Archived"
        };

        for (String status : statuses) {
            ticketsByStatus.put(status, 0);
        }

        for (Ticket ticket : tickets) {
            String status = ticket.getStatus();
            ticketsByStatus.put(status, ticketsByStatus.getOrDefault(status, 0) + 1);
        }

        return ticketsByStatus;
    }

    @GetMapping("/by-year")
    public Map<String, Integer> getTicketsByYear(@RequestParam int year) {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Integer> ticketsByMonth = new HashMap<>();

        String[] orderedMonths = {
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        };

        for (String month : orderedMonths) {
            ticketsByMonth.put(month, 0);
        }

        for (Ticket ticket : tickets) {
            LocalDateTime createdAt = ticket.getCreatedAt();
            if (createdAt != null && createdAt.getYear() == year) {
                String month = createdAt.getMonth().toString();
                ticketsByMonth.put(month, ticketsByMonth.getOrDefault(month, 0) + 1);
            }
        }

        return ticketsByMonth;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Endpoint pour récupérer tous les tickets
    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> findAll() {
        List<Ticket> tickets = ticketService.findAll();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer ticketId) {
        ticketService.deleteTicketById(ticketId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/montant")
    public ResponseEntity<MontantTicket> getMontantTicket(@PathVariable int id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isPresent()) {
            MontantTicket montantTicket = montantTicketService.findByTicket(ticket.get()).stream().findFirst().orElse(null);
            if (montantTicket != null) {
                return ResponseEntity.ok(montantTicket);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("/{id}/montant")
    public ResponseEntity<?> updateMontantTicket(@PathVariable int id, @RequestBody Map<String, String> request) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isPresent()) {
            MontantTicket montantTicket = montantTicketService.findByTicket(ticket.get()).stream().findFirst().orElse(null);
            if (montantTicket != null) {
                montantTicket.setMontant(new BigDecimal(request.get("montant")));
                montantTicketService.save(montantTicket);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket non trouvé");
    }
}
