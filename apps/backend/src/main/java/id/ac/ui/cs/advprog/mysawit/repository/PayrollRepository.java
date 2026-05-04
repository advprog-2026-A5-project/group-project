package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Payroll;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByUserId(Long userId);
    List<Payroll> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
