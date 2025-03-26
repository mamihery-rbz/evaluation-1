package site.easy.to.build.crm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;

import java.util.List;
import java.util.Map;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Integer> {
    public Lead findByLeadId(int id);

    public List<Lead> findByCustomerCustomerId(int customerId);
    public List<Lead> findByManagerId(int userId);

    public List<Lead> findByEmployeeId(int userId);

    Lead findByMeetingId(String meetingId);

    public List<Lead> findByEmployeeIdOrderByCreatedAtDesc(int employeeId, Pageable pageable);

    public List<Lead> findByManagerIdOrderByCreatedAtDesc(int managerId, Pageable pageable);

    public List<Lead> findByCustomerCustomerIdOrderByCreatedAtDesc(int customerId, Pageable pageable);

    long countByEmployeeId(int employeeId);

    long countByManagerId(int managerId);
    long countByCustomerCustomerId(int customerId);

    void deleteAllByCustomer(Customer customer);

    @Query("SELECT l.status, COUNT(l) FROM Lead l GROUP BY l.status")
    List<Object[]> countLeadsByStatus();
}
