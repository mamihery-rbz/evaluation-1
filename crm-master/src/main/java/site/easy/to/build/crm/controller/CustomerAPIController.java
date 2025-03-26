package site.easy.to.build.crm.controller;

import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.entity.Customer;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerAPIController {
    private final CustomerRepository customerRepository;

    public CustomerAPIController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/by-year")
    public Map<String, Integer> getCustomersByYear(@RequestParam int year) {
        List<Customer> customers = customerRepository.findAll();

        // Liste ordonnée des mois
        String[] orderedMonths = {
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        };

        // Initialiser la Map avec les mois dans l'ordre
        Map<String, Integer> customersByMonth = new HashMap<>();
        for (String month : orderedMonths) {
            customersByMonth.put(month, 0);
        }

        // Compter les clients par mois
        for (Customer customer : customers) {
            LocalDateTime createdAt = customer.getCreatedAt();
            if (createdAt != null && createdAt.getYear() == year) {
                String month = createdAt.getMonth().toString();
                customersByMonth.put(month, customersByMonth.getOrDefault(month, 0) + 1);
            }
        }

        // Retourner une Map ordonnée
        Map<String, Integer> orderedCustomersByMonth = new LinkedHashMap<>();
        for (String month : orderedMonths) {
            orderedCustomersByMonth.put(month, customersByMonth.get(month));
        }

        return orderedCustomersByMonth;
    }

    @GetMapping("/total")
    public int getTotalCustomers() {
        return customerRepository.findAll().size();
    }
}