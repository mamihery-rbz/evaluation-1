package site.easy.to.build.crm.service.lead;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.MontantLead;
import site.easy.to.build.crm.entity.Lead;

import java.util.List;

@Service
public class MontantLeadServiceImpl implements MontantLeadService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public MontantLead findByMontantLeadId(int id) {
        return entityManager.find(MontantLead.class, id);
    }

    @Override
    @Transactional
    public MontantLead save(MontantLead montantLead) {
        if (montantLead.getIdMontantLead() == 0) {
            entityManager.persist(montantLead);
        } else {
            entityManager.merge(montantLead);
        }
        return montantLead;
    }

    @Override
    @Transactional
    public void delete(MontantLead montantLead) {
        entityManager.remove(entityManager.contains(montantLead) ? montantLead : entityManager.merge(montantLead));
    }

    @Override
    public List<MontantLead> findAll() {
        return entityManager.createQuery("SELECT m FROM MontantLead m", MontantLead.class).getResultList();
    }

    @Override
    public List<MontantLead> findByLead(Lead lead) {
        return entityManager.createQuery(
                        "SELECT m FROM MontantLead m WHERE m.lead = :lead", MontantLead.class)
                .setParameter("lead", lead)
                .getResultList();  // Retourne un MontantLead unique
    }

    @Override
    @Transactional
    public void deleteByLead(Lead lead) {
        entityManager.createQuery("DELETE FROM MontantLead m WHERE m.lead = :lead")
                .setParameter("lead", lead)
                .executeUpdate();
    }
}