package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.notification.NotificationService;
import site.easy.to.build.crm.service.ticket.TicketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/depenses")
public class DepenseController {


    @Autowired
    private final BudgetService budgetService;

    @Autowired
    private final LeadService leadService;

    @Autowired
    private final TicketService ticketService;

    @Autowired
    private final BudgetRepository budgetRepository;

    @Autowired
    private final DepenseRepository depenseRepository;

    @Autowired
    private final DepenseService depenseService;

    @Autowired
    private final NotificationService notificationService;

    public DepenseController(BudgetService budgetService, TicketService ticketService, LeadService leadService, BudgetRepository budgetRepository, DepenseRepository depenseRepository, DepenseService depenseService, NotificationService notificationService) {
        this.budgetService = budgetService;
        this.ticketService = ticketService;
        this.leadService = leadService;
        this.budgetRepository = budgetRepository;
        this.depenseRepository = depenseRepository;
        this.depenseService = depenseService;
        this.notificationService = notificationService;
    }

    @GetMapping("/form-expense/{id}/{id-customer}")
    public String formAddExpense(Model model, @PathVariable("id") int id, @PathVariable("id-customer") int id_customer) {
        Lead lead = leadService.findByLeadId(id);
        Depense depense = depenseService.findByLead(lead);
        if (depense != null){
            return "redirect:/employee/lead/manager/all-leads";
        }
        model.addAttribute("id_lead", id);
        model.addAttribute("id_customer", id_customer);
        model.addAttribute("depense", new Depense());
        return "lead/form";
    }

    @GetMapping("/form-expense/ticket/{id}/{id-customer}")
    public String formAddExpenseTicket(Model model, @PathVariable("id") int id, @PathVariable("id-customer") int id_customer) {
        Ticket lead = ticketService.findByTicketId(id);
        Depense depense = depenseService.findByTicket(lead);
        if (depense != null){
            return "redirect:/employee/ticket/manager/all-tickets";
        }
        model.addAttribute("id_ticket", id);
        model.addAttribute("id_customer", id_customer);
        return "ticket/form";
    }


    @GetMapping("/expenses")
    public String showAddExpensePage(Model model) {
        List<Budget> budgets = budgetService.findAll();
        List<Ticket> tickets = ticketService.findAll();
        List<Lead> leads = leadService.findAll();

        Map<Integer, BigDecimal> groupedBudgets = budgets.stream()
                .collect(Collectors.groupingBy(
                        budget -> budget.getCustomer().getCustomerId(),
                        Collectors.reducing(BigDecimal.ZERO, Budget::getMontant, BigDecimal::add)
                ));

        System.out.println(groupedBudgets);


        model.addAttribute("budgets", groupedBudgets);
        model.addAttribute("tickets", tickets);
        model.addAttribute("leads", leads);
        model.addAttribute("budget", budgetService.findAll());
        model.addAttribute("depense", new Depense());

        return "depense/create-depense";
    }



//    @PostMapping("/create/ticket")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> addTicketExpense(
//                                                          @RequestParam("id_ticket") int ticketId,
//                                                          @RequestParam("id_customer") int customerId,
//                                                          @RequestParam("montant") double montant,
//                                                          @RequestParam("date") String dateCreation) {
//        Map<String, Object> response = new HashMap<>();
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
//        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);
//
//        System.out.println("Customer : "+customerId);
//        double sommeBudgetCustomer = budgetRepository.getTotalBudgetByCustomerId(customerId);
//        System.out.println("Somme budget Customer : "+sommeBudgetCustomer);
//        double sommeDepenseCustomer = depenseRepository.getTotalDepenseByCustomerId(customerId) + montant;
//        System.out.println("Somme depense Customer : "+sommeDepenseCustomer);
//
//
//        Notification messageVerification = budgetService.verifyBudget(customerId, sommeBudgetCustomer);
//        if (messageVerification == 1) {
//            response.put("status", 1);
//            response.put("message", "Le budget approche de son seuil !");
//        } else if (messageVerification == 2) {
//            response.put("status", 2);
//            response.put("message", "Le budget est dépassé !");
//        } else {
//            Depense depense = new Depense();
//            Ticket ticket = ticketService.findByTicketId(ticketId);
//            depense.setTicket(ticket);
//            depense.setMontant(BigDecimal.valueOf(montant));
//            depense.setCreated_at(dateTime);
//
//            depenseRepository.save(depense);
//
//            response.put("status", 0);
//            response.put("message", "Dépense ajoutée avec succès !");
//        }
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/create/ticket")
    @ResponseBody
    public String addTicketExpense(
            @RequestParam("id_ticket") int ticketId,
            @RequestParam("id_customer") int customerId,
            @RequestParam("montant") double montant,
            @RequestParam("date") String dateCreation) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        System.out.println("Customer : "+customerId);
        double sommeBudgetCustomer = budgetRepository.getTotalBudgetByCustomerId(customerId);
        System.out.println("Somme budget Customer : "+sommeBudgetCustomer);
        double sommeDepenseCustomer = depenseRepository.getTotalDepenseByCustomerId(customerId) + montant;
        System.out.println("Somme depense Customer : "+sommeDepenseCustomer);

        Ticket ticket = ticketService.findByTicketId(ticketId);
        Notification notification = budgetService.verifyBudget(customerId, montant);
        depense.setTicket(ticket);
        depense.setMontant(BigDecimal.valueOf(montant));
        depense.setCreated_at(dateTime);
        notification.setDateNotification(dateTime);
        depense.setEtat(notification.getEtat());

        Depense depense1 = depenseService.save(depense);
        notification.setEtat(0);
        notification.setIdDepense(depense1.getId().intValue());
        System.out.println("notif.getMessage()" + notification.getMessage());
        if (!notification.getMessage().equals("successful")) {
            notificationService.save(notification);
        }

        return "redirect:/employee/ticket/manager/all-tickets";
    }


    @PostMapping("/create/lead")
    @ResponseBody
    public String addLeadExpense(
                                                                @RequestParam("id_lead") int leadId,
                                                                @RequestParam("id_customer") int customerId,
                                                                @RequestParam("montant") double montant,
                                                                @RequestParam("date") String dateCreation) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        System.out.println("Customer : "+customerId);
        double sommeBudgetCustomer = budgetRepository.getTotalBudgetByCustomerId(customerId);
        System.out.println("Somme budget Customer : "+sommeBudgetCustomer);
        double sommeDepenseCustomer = depenseRepository.getTotalDepenseByCustomerId(customerId) + montant;
        System.out.println("Somme depense Customer : "+sommeDepenseCustomer);

        Lead lead = leadService.findByLeadId(leadId);
        Notification notification = budgetService.verifyBudget(customerId, montant);
        depense.setLead(lead);
        depense.setMontant(BigDecimal.valueOf(montant));
        depense.setCreated_at(dateTime);
        notification.setDateNotification(dateTime);
        depense.setEtat(notification.getEtat());

        Depense depense1 = depenseService.save(depense);
        notification.setEtat(0);
        notification.setIdDepense(depense1.getId().intValue());
        System.out.println("notif.getMessage()" + notification.getMessage());
        if (!notification.getMessage().equals("successful")) {
            notificationService.save(notification);
        }

        return "redirect:/employee/ticket/manager/all-tickets";
    }


    @PostMapping("/create/lead/confirmation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addExpenseLeadConfirmation(   @RequestParam("id_lead") int leadId,
                                                                             @RequestParam("id_customer") int customerId,
                                                                             @RequestParam("montant") double montant,
                                                                             @RequestParam("date") String dateCreation) {
        Map<String, Object> response = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Depense depense = new Depense();
        Lead lead = leadService.findByLeadId(leadId);
        depense.setLead(lead);
        depense.setMontant(BigDecimal.valueOf(montant));
        depense.setCreated_at(dateTime);

        depenseRepository.save(depense);

        response.put("status", 0);
        response.put("message", "Dépense ajoutée avec succès !");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/ticket/confirmation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addExpenseTicketConfirmation(  @RequestParam("id_ticket") int ticketId,
                                                                        @RequestParam("montant") double montant,
                                                                              @RequestParam("id_customer") int customerId,
                                                                        @RequestParam("date") String dateCreation) {
        Map<String, Object> response = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Depense depense = new Depense();
        Ticket ticket = ticketService.findByTicketId(ticketId);
        depense.setTicket(ticket);
        depense.setMontant(BigDecimal.valueOf(montant));
        depense.setCreated_at(dateTime);

        depenseRepository.save(depense);

        response.put("status", 0);
        response.put("message", "Dépense ajoutée avec succès !");

        return ResponseEntity.ok(response);
    }




    @GetMapping("/depenses/list")
    public String listDepense(Model model) {
        List<Depense> depenses = depenseService.findAll();
        model.addAttribute("depenses", depenses);

        return "depense/list-depense";
    }

}
