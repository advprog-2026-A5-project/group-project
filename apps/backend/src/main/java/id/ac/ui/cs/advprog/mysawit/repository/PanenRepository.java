package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Panen;
import id.ac.ui.cs.advprog.mysawit.model.StatusPanen;
import id.ac.ui.cs.advprog.mysawit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PanenRepository extends JpaRepository<Panen, Long> {
    boolean buruh
}
