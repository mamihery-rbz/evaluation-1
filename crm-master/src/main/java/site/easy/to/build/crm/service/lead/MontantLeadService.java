package site.easy.to.build.crm.service.lead;

import site.easy.to.build.crm.entity.MontantLead;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.MontantTicket;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;

public interface MontantLeadService {
    MontantLead findByMontantLeadId(int id);

    MontantLead save(MontantLead montantLead);

    void delete(MontantLead montantLead);

    List<MontantLead> findAll();

    List<MontantLead> findByLead(Lead lead); // Retourne un MontantLead unique

    void deleteByLead(Lead lead);
}