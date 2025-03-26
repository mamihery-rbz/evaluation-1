package site.easy.to.build.crm.service.depense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.TauxRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepenseService {


    @Autowired
    private DepenseRepository depenseRepository;

    public List<Depense> findAll() {
        return depenseRepository.findAll();
    }


    public Depense findByLead(Lead leadId) {
        return depenseRepository.findByLead(leadId);
    }

    public Depense findByTicket(Ticket ticket) {
        return depenseRepository.findByTicket(ticket);
    }

    public Optional<Depense> findById(Long id) {
        return depenseRepository.findById(id);
    }

    public Depense save(Depense depense) {
        return depenseRepository.save(depense);
    }

    public void delete(Long id) {
        depenseRepository.deleteById(id);
    }

    public List<Depense> findAllWithLeads() {
        return depenseRepository.findAll().stream()
                .filter(depense -> depense.getLead() != null)
                .collect(Collectors.toList());
    }

    public List<Depense> findAllWithTickets() {
        return depenseRepository.findAll().stream()
                .filter(depense -> depense.getTicket() != null)
                .collect(Collectors.toList());
    }

}