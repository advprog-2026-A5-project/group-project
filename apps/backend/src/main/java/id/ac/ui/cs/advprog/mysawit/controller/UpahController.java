package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.UpahRequestDTO;
import id.ac.ui.cs.advprog.mysawit.dto.UpahResponseDTO;
import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.service.UpahService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upah")
public class UpahController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final UpahService upahService;

    public UpahController(UpahService upahService) {
        this.upahService = upahService;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestHeader("X-User-Role") String userRole) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            List<UpahResponseDTO> responses = upahService.getAll().stream()
                    .map(this::toResponse)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> update(
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody UpahRequestDTO request) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            Upah updated = upahService.update(request);
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    private UpahResponseDTO toResponse(Upah upah) {
        return new UpahResponseDTO(
                upah.getRole(),
                upah.getUpahPerKg()
        );
    }

    private void ensureRole(String actualRole, String expectedRole) {
        if (!isRole(actualRole, expectedRole)) {
            throw new SecurityException("Only " + expectedRole + " can access this endpoint");
        }
    }

    private boolean isRole(String actualRole, String expectedRole) {
        return actualRole != null && actualRole.equalsIgnoreCase(expectedRole);
    }
}
