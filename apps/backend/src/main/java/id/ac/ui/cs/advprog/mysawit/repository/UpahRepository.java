package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpahRepository extends JpaRepository<Upah, Long> {
	Optional<Upah> findByRole(UpahRole role);
}
