package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Panen;
import id.ac.ui.cs.advprog.mysawit.model.StatusPanen;
import id.ac.ui.cs.advprog.mysawit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PanenRepository extends JpaRepository<Panen, Long> {
    boolean existLaporanByBuruhAndTanggalPanen(
        User buruh,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );

    List<Panen> findByBuruh(User buruh);

    List<Panen> findByBuruhbetweenTanggal(
        User buruh,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    List<Panen> findBystatus(StatusPanen status);
}

