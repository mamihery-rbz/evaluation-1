package site.easy.to.build.crm.service.ticket;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.MontantTicket;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;

@Service
public class MontantTicketServiceImpl implements MontantTicketService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public MontantTicket findByMontantTicketId(int id) {
        return entityManager.find(MontantTicket.class, id);
    }

    @Override
    @Transactional
    public MontantTicket save(MontantTicket montantTicket) {
        if (montantTicket.getIdMontantTicket() == 0) {
            entityManager.persist(montantTicket);
        } else {
            entityManager.merge(montantTicket);
        }
        return montantTicket;
    }

    @Override
    @Transactional
    public void delete(MontantTicket montantTicket) {
        entityManager.remove(entityManager.contains(montantTicket) ? montantTicket : entityManager.merge(montantTicket));
    }

    @Override
    public List<MontantTicket> findAll() {
        return entityManager.createQuery("SELECT m FROM MontantTicket m", MontantTicket.class).getResultList();
    }

    @Override
    public List<MontantTicket> findByTicket(Ticket ticket) {
        return entityManager.createQuery(
                        "SELECT m FROM MontantTicket m WHERE m.ticket = :ticket", MontantTicket.class)
                .setParameter("ticket", ticket)
                .getResultList();  // Retourne une liste de MontantTicket
    }

    @Override
    @Transactional
    public void deleteByTicket(Ticket ticket) {
        entityManager.createQuery("DELETE FROM MontantTicket m WHERE m.ticket = :ticket")
                .setParameter("ticket", ticket)
                .executeUpdate();
    }
}