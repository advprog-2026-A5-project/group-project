package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.AssignDriverRequest;
import id.ac.ui.cs.advprog.mysawit.dto.UpdatePengirimanStatusRequest;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import id.ac.ui.cs.advprog.mysawit.service.PengirimanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pengiriman")
@RequiredArgsConstructor
public class PengirimanController {

    private final PengirimanService pengirimanService;

    /**
     * Mandor assigns a driver to transport approved harvests.
     * Validates total weight <= 400 Kg.
     */
    @PostMapping("/assign")
    public ResponseEntity<?> assignDriver(
            @RequestHeader("X-User-Id") UUID mandorId,
            @RequestBody AssignDriverRequest request) {
        try {
            Pengiriman pengiriman = pengirimanService.assignDriver(mandorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pengiriman);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Driver updates pengiriman status following state machine:
     * MEMUAT -> MENGIRIM -> TIBA_DI_TUJUAN
     */
    @PatchMapping("/{pengirimanId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID pengirimanId,
            @RequestHeader("X-User-Id") UUID driverId,
            @RequestBody UpdatePengirimanStatusRequest request) {
        try {
            Pengiriman pengiriman = pengirimanService.updatePengirimanStatus(
                    pengirimanId, driverId, request.getNewStatus());
            return ResponseEntity.ok(pengiriman);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Get pengiriman  assigned to a specific driver.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Pengiriman>> getPengirimanByDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(pengirimanService.getPengirimanByDriver(driverId));
    }

    /**
     * Mandor views ongoing pengiriman.
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<Pengiriman>> getOngoingPengiriman(
            @RequestHeader("X-User-Id") UUID mandorId) {
        return ResponseEntity.ok(pengirimanService.getOngoingPengiriman(mandorId));
    }

    /**
     * Get a specific pengiriman by ID.
     */
    @GetMapping("/{pengirimanId}")
    public ResponseEntity<?> getPengirimanById(@PathVariable UUID pengirimanId) {
        try {
            return ResponseEntity.ok(pengirimanService.getPengirimanById(pengirimanId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}