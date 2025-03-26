package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taux_alert")
public class Taux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taux_id")
    private int tauxId;

    @Column(name = "valeur", nullable = false)
    private BigDecimal valeur;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Ajoutez ce champ

    public int getTauxId() {
        return tauxId;
    }

    public void setTauxId(int tauxId) {
        this.tauxId = tauxId;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }
}
