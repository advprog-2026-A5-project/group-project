package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.UpahRequestDTO;
import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.repository.UpahRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class UpahServiceImpl implements UpahService {

    private final UpahRepository upahRepository;

    public UpahServiceImpl(UpahRepository upahRepository) {
        this.upahRepository = upahRepository;
    }

    @PostConstruct
    public void ensureRolesExist() {
        Arrays.stream(UpahRole.values()).forEach(this::ensureRoleExists);
    }

    @Override
    public List<Upah> getAll() {
        return upahRepository.findAll();
    }

    @Override
    @Transactional
    public Upah update(UpahRequestDTO request) {
        Upah existing = upahRepository.findByRole(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Upah untuk role tidak ditemukan"));
        existing.setUpahPerKg(request.getUpahPerKg());
        return upahRepository.save(existing);
    }

    private void ensureRoleExists(UpahRole role) {
        if (upahRepository.findByRole(role).isEmpty()) {
            Upah initial = new Upah();
            initial.setRole(role);
            initial.setUpahPerKg(0.0);
            upahRepository.save(initial);
        }
    }
}
