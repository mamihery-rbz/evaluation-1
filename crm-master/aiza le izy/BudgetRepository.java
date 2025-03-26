package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Contract;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    List<Budget> findByCustomer_CustomerId(Long customerId);

    Optional<Budget> findById(int id);

    @Query("SELECT b FROM Budget b WHERE b.customer.customerId = :customerId")
    Optional<Budget> findBudgetByCustomerId(@Param("customerId") int customerId);


    List<Budget> findAll();

    Budget save(Budget budget);

//    List<Budget> findBudgetByIdCu(int clientId);


    @Query(value = "SELECT b.id,b.created_at, c.customer_id,c.name as customerName, " +
            "b.montant as montant " +
            "FROM budget b INNER JOIN customer c " +
                "ON b.customer_id = c.customer_id " +
            "where c.customer_id = ?;", nativeQuery = true)
    List<Budget> findCustomerBudgetsNative(int idBudget);

    @Query("SELECT COALESCE(SUM(b.montant), 0) FROM Budget b WHERE b.customer.customerId = :customerId")
    double getTotalBudgetByCustomerId(@Param("customerId") int customerId);

    List<Budget> findByCustomerCustomerId(int userId);


}
