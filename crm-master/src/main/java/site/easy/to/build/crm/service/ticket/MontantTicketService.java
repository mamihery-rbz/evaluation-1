package site.easy.to.build.crm.service.ticket;

import site.easy.to.build.crm.entity.MontantTicket;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;

public interface MontantTicketService {
    MontantTicket findByMontantTicketId(int id);

    MontantTicket save(MontantTicket montantTicket);

    void delete(MontantTicket montantTicket);

    List<MontantTicket> findAll();

    List<MontantTicket> findByTicket(Ticket ticket);  // Retourne une liste de MontantTicket

    void deleteByTicket(Ticket ticket);
}