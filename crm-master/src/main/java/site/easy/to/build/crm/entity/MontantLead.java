package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "montant_lead")
public class MontantLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_montant_lead")
    private int idMontantLead;

    @Column(name = "montant", precision = 15, scale = 2)
    @NotNull(message = "Montant is required")
    private BigDecimal montant;

    @OneToOne
    @JoinColumn(name = "lead_id", referencedColumnName = "lead_id", unique = true)
    private Lead lead;

    public MontantLead() {
    }

    public MontantLead(BigDecimal montant, Lead lead) {
        this.montant = montant;
        this.lead = lead;
    }

    // Getters et Setters
    public int getIdMontantLead() {
        return idMontantLead;
    }

    public void setIdMontantLead(int idMontantLead) {
        this.idMontantLead = idMontantLead;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }
}