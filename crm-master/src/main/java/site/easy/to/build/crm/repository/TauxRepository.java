package site.easy.to.build.crm.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Taux;

import java.util.Optional;

@Repository
public interface TauxRepository extends JpaRepository<Taux, Integer> {

    @Query("SELECT t FROM Taux t ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Taux> findLatestTaux();

    Optional<Taux> findTopByOrderByCreatedAtDesc();
}
