package site.easy.to.build.crm.service.budget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.TauxRepository;
import site.easy.to.build.crm.service.customer.CustomerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DepenseRepository depenseRepository;

    @Autowired
    private TauxRepository tauxRepository;

    private final CustomerService customerService;

    public BudgetService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsByCustomer(Long customerId) {
        return budgetRepository.findByCustomer_CustomerId(customerId);
    }

//    public Depense addExpense(int budgetId, Depense depense) {
//        Budget budget = budgetRepository.findById(budgetId)
//                .orElseThrow(() -> new RuntimeException("Budget non trouvé"));
//        depense.setBudget(budget);
//        return depenseRepository.save(depense);
//    }



    public Notification verifyBudget(int customerId, double montantDepense) {
        double sommeBudgetCustomer = budgetRepository.getTotalBudgetByCustomerId(customerId);
        double sommeDepenseCustomer = depenseRepository.getTotalDepenseByCustomerId(customerId) + montantDepense;
        Optional<Taux> taux = tauxRepository.findLatestTaux();

        BigDecimal seuilAlerte = taux.get().getValeur();

        BigDecimal seuilAlerteMontant = valeurTaux(seuilAlerte, BigDecimal.valueOf(sommeBudgetCustomer));

        LocalDateTime date= LocalDateTime.now();
        Customer cust= customerService.findByCustomerId(customerId);

        if (sommeDepenseCustomer > seuilAlerteMontant.doubleValue()) {
            return new Notification("Le seuil du budget est dépassé", date, 0, cust);
        }
        else if (sommeDepenseCustomer > sommeBudgetCustomer) {
            return new Notification("Le budget est dépassé", date, 0, cust);
        }
        else if (sommeDepenseCustomer == sommeBudgetCustomer) {
            return new Notification("le seuil du budget est atteint", date, 1, cust);
        }
        else{
            return new Notification("successful", date, 1, cust);
        }

    }



//    public BigDecimal calculerMontantTotalDepenses(Budget budget, Depense depense) {
//        List<Depense> depenses = depenseRepository.findByBudget(budget);
//        BigDecimal sommeDepenses = depenses.stream()
//                .map(Depense::getMontant)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal montantTotal = depense.getMontant().add(sommeDepenses);
//
//        return montantTotal;
//    }

//    SELECT COALESCE(SUM(d.montant), 0)
//    FROM depense d
//    LEFT JOIN trigger_lead l ON d.lead_id = l.lead_id
//    LEFT JOIN trigger_ticket t ON d.ticket_id = t.ticket_id
//    LEFT JOIN customer c ON l.customer_id = c.customer_id
//    LEFT JOIN customer c2 ON t.customer_id = c2.customer_id
//    WHERE c.customer_id = 1 OR c2.customer_id = 1;



//    public BigDecimal calculerMontantTotalDepenses(List<Budget> budgets, Depense nouvelleDepense) {
//        BigDecimal sommeDepenses = BigDecimal.ZERO;
//
//        for (Budget budget : budgets) {
//            List<Depense> depenses = depenseRepository.findByBudget(budget);
//            BigDecimal totalBudgetDepenses = depenses.stream()
//                    .map(Depense::getMontant)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            sommeDepenses = sommeDepenses.add(totalBudgetDepenses);
//        }
//
//        return sommeDepenses.add(nouvelleDepense.getMontant());
//    }


    public BigDecimal valeurTaux(BigDecimal seuilAlerte, BigDecimal montantBudget) {
        BigDecimal seuilAlerteMontant = montantBudget.multiply(seuilAlerte).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
        return seuilAlerteMontant;
    }

    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public Optional<Budget> findById(int budgetId) {
        return budgetRepository.findById(budgetId);
    }

    public double getTotalBudgetByCustomerId(int customerId) {
        return budgetRepository.getTotalBudgetByCustomerId(customerId);
    }

    public void save(Budget budget) {
        budgetRepository.save(budget);
    }
}