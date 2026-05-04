package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.PayrollRequestDTO;
import id.ac.ui.cs.advprog.mysawit.dto.PayrollResponseDTO;
import id.ac.ui.cs.advprog.mysawit.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.service.PayrollService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("X-User-Role") String userRole) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            List<PayrollResponseDTO> responses = payrollService.getAll().stream()
                    .map(this::toResponse)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable Long id) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            Payroll payroll = payrollService.getById(id);
            return ResponseEntity.ok(toResponse(payroll));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // TODO: Add getAll and getById for nonadmins

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody PayrollRequestDTO request) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            Payroll created = payrollService.createWithKilogram(
                    request.getUserId(),
                    request.getRole(),
                    request.getKilogram()
            );
            return ResponseEntity.ok(toResponse(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // TODO: Add create for nonadmins

    private PayrollResponseDTO toResponse(Payroll payroll) {
        return new PayrollResponseDTO(
                payroll.getId(),
                payroll.getUserId(),
                payroll.getStatus(),
                payroll.getAmount(),
                payroll.getCreatedAt()
        );
    }

    // TODO: Add update status

    private void ensureRole(String actualRole, String expectedRole) {
        if (!isRole(actualRole, expectedRole)) {
            throw new SecurityException("Only " + expectedRole + " can access this endpoint");
        }
    }

    private boolean isRole(String actualRole, String expectedRole) {
        return actualRole != null && actualRole.equalsIgnoreCase(expectedRole);
    }
}
