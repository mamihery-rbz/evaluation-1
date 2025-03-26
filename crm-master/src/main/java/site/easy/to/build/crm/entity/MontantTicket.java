package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "montant_ticket")
public class MontantTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_montant_ticket")
    private int idMontantTicket;

    @Column(name = "montant", precision = 15, scale = 2)
    @NotNull(message = "Montant is required")
    private BigDecimal montant;

    @OneToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "ticket_id", unique = true)
    private Ticket ticket;

    public MontantTicket() {
    }

    public MontantTicket(BigDecimal montant, Ticket ticket) {
        this.montant = montant;
        this.ticket = ticket;
    }

    // Getters et Setters
    public int getIdMontantTicket() {
        return idMontantTicket;
    }

    public void setIdMontantTicket(int idMontantTicket) {
        this.idMontantTicket = idMontantTicket;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}