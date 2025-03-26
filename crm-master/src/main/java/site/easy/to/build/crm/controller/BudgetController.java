package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private final BudgetService budgetService;

    @Autowired
    private final BudgetRepository budgetRepository;

    @Autowired
    private final CustomerService customerService;

    private final AuthenticationUtils authenticationUtils;


    public BudgetController(BudgetService budgetService, BudgetRepository budgetRepository, CustomerService customerService, AuthenticationUtils authenticationUtils) {
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
        this.customerService = customerService;
        this.authenticationUtils = authenticationUtils;
    }

    @GetMapping("/create/page")
    public String pageCreateBudget(Model model) {
        model.addAttribute("budget", new Budget());
        return "budget/create";
    }

    @PostMapping("/create")
    public String saveBudget(@ModelAttribute Budget budget,Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        Customer customer = customerService.findByCustomerId(userId);
        double cumul = budgetService.getTotalBudgetByCustomerId(userId);
        System.out.println("user id : "+userId);
        System.out.println("Cumul : "+cumul);

        budget.setMontant(BigDecimal.valueOf(cumul).add(budget.getMontant()));

        budget.setCustomer(customer);
        budgetService.save(budget);

        return "redirect:/budgets/create/page";
    }

    @GetMapping("/budgets/list")
    public String listBudget(Model model) {
        List<Budget> budgets = budgetService.findAll();
        model.addAttribute("budgets", budgets);

        return "budget/list-budget";
    }

    @GetMapping("/customerBudget")
    public String showCreatedCustomerBudget(Authentication authentication,Model model){
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Budget> budgets = budgetRepository.findByCustomerCustomerId(userId);
        double valeur = budgetService.getTotalBudgetByCustomerId(userId);

        model.addAttribute("budgets",budgets);
        model.addAttribute("valeur",valeur);
        return "budget/myBudget";
    }

    /////////////////////////////////////////////////////////////

    @GetMapping("/customer/{customerId}")
    public List<Budget> getBudgetsByCustomer(@PathVariable Long customerId) {
        return budgetService.getBudgetsByCustomer(customerId);
    }

//    @PostMapping("/{budgetId}/expenses")
//    public ResponseEntity<String> addExpense(@PathVariable int budgetId, @RequestBody Depense depense) {
//
//        int messageVerification = budgetService.verifierBudget(budgetId, depense);
//
//        if(messageVerification == 1){
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("{\"message\": \"Le budget approche de son seuil !\", " +
//                            "\"valeur\": "+ messageVerification +
//                            " }");
//        }
//        if(messageVerification == 2){
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body("{\"message\": \"Le budget est dépassé !\", " +
//                            "\"valeur\": "+ messageVerification +
//                            " }");
//        }
//
//        Depense createdDepense = budgetService.addExpense(budgetId, depense);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body("{\"message\": \"Ajout depense mety!...\", " +
//                        "\"valeur\": "+messageVerification +
//                " }");
//    }

//    @GetMapping("/{budgetId}/check")
//    public ResponseEntity<Integer> checkBudget(@PathVariable int budgetId) {
//        int message = budgetService.verifierBudget(budgetId);
//        return new ResponseEntity<>(message, HttpStatus.OK);
//    }
}
