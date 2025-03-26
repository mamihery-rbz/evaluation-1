package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private LeadRepository leadRepository;

    @GetMapping("/customers")
    public List<Object[]> getCustomerStats() {
        return customerRepository.countCustomersByDate();
    }

    @GetMapping("/totalCustomers")
    public int getTotalCustomers() {
        return customerRepository.findAll().size();
    }

    @GetMapping("/tickets")
    public List<Object[]> getTicketStats() {
        return ticketRepository.countTicketsByStatus();
    }

    @GetMapping("/totalTickets")
    public int getTotalTicket() {
        return ticketRepository.findAll().size();
    }

    @GetMapping("/leads")
    public List<Object[]> getLeadStats() {
        return leadRepository.countLeadsByStatus();
    }

    @GetMapping("/totalLeads")
    public int getTotalLead() {
        return leadRepository.findAll().size();
    }


}
