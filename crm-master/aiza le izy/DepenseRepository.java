package site.easy.to.build.crm.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;
import java.util.Optional;

public interface DepenseRepository extends JpaRepository<Depense, Integer> {


    Depense findByLead(Lead lead);
    Depense findByTicket(Ticket ticket);

    Optional<Depense> findById(Long id);
    void deleteById(Long id);


    @Query("SELECT COALESCE(SUM(d.montant), 0) " +
            "FROM Depense d " +
            "LEFT JOIN d.lead l " +
            "LEFT JOIN d.ticket t " +
            "LEFT JOIN l.customer c " +
            "LEFT JOIN t.customer c2 " +
            "WHERE (c.customerId = :customerId OR c2.customerId = :customerId) AND d.etat = 1")
    double getTotalDepenseByCustomerId(@Param("customerId") int customerId);

    @Query("SELECT d FROM Depense d WHERE d.ticket IS NOT NULL")
    List<Depense> findAllDepensesForTickets();

    @Query("SELECT d FROM Depense d WHERE d.lead IS NOT NULL")
    List<Depense> findAllDepensesForLeads();
    @Query("SELECT COALESCE(SUM(d.montant), 0) FROM Depense d WHERE d.ticket IS NOT NULL")
    double getTotalDepenseForTickets();

    @Query("SELECT COALESCE(SUM(d.montant), 0) FROM Depense d WHERE d.lead IS NOT NULL")
    double getTotalDepenseForLeads();

    @Modifying
    @Transactional
    @Query("DELETE FROM Depense d WHERE d.lead.leadId = :leadId")
    void deleteByLeadId(@Param("leadId") int leadId);

    @Modifying
    @Transactional
    @Query("UPDATE Depense d SET d.montant = :montantDepense WHERE d.id = :depenseId")
    void updateById(@Param("depenseId") int depenseId, @Param("montantDepense") double montantDepense);

//    Ticket findByTicketId(int ticketId);
}
