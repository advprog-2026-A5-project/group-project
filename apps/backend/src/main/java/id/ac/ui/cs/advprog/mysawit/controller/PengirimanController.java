package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.AssignDriverRequest;
import id.ac.ui.cs.advprog.mysawit.dto.AdminPartialRejectionRequest;
import id.ac.ui.cs.advprog.mysawit.dto.AdminRejectionRequest;
import id.ac.ui.cs.advprog.mysawit.dto.MandorRejectionRequest;
import id.ac.ui.cs.advprog.mysawit.dto.UpdateStatusPengirimanRequest;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import id.ac.ui.cs.advprog.mysawit.model.User;
import id.ac.ui.cs.advprog.mysawit.service.PengirimanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pengiriman")
@RequiredArgsConstructor
public class PengirimanController {

    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_DRIVER = "DRIVER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final PengirimanService pengirimanService;

    /**
     * Mandor assigns a driver to transport approved harvests.
     * Validates total weight <= 400 Kg.
     */
    @PostMapping("/assign")
    public ResponseEntity<?> assignDriver(
            @RequestHeader("X-User-Id") Long mandorId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody AssignDriverRequest request) {
        try {
            ensureRole(userRole, ROLE_MANDOR);
            Pengiriman pengiriman = pengirimanService.assignDriver(mandorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pengiriman);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Driver updates status pengiriman: MEMUAT -> MENGIRIM -> TIBA_DI_TUJUAN*/
    @PatchMapping("/{pengirimanId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long driverId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody UpdateStatusPengirimanRequest request) {
        try {
            ensureRole(userRole, ROLE_DRIVER);
            Pengiriman pengiriman = pengirimanService.updateStatusPengiriman(
                    pengirimanId, driverId, request.getNewStatus());
            return ResponseEntity.ok(pengiriman);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Ambil pengiriman dari driver tertentu.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getPengirimanByDriver(
            @PathVariable Long driverId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String userRole) {
        try {
            if (isRole(userRole, ROLE_DRIVER)) {
                if (!driverId.equals(requesterId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Driver can only access their own shipments");
                }
                return ResponseEntity.ok(pengirimanService.getPengirimanByDriver(driverId));
            }

            if (isRole(userRole, ROLE_MANDOR)) {
                return ResponseEntity.ok(pengirimanService.getPengirimanByDriverForMandor(requesterId, driverId));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only MANDOR or DRIVER can access this endpoint");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* Riwayat pengiriman milik supir, bisa filter tanggal. */
    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<?> getPengirimanHistoryByDriver(
            @PathVariable Long driverId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ensureRole(userRole, ROLE_DRIVER);
            if (!driverId.equals(requesterId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Driver can only access their own shipment history");
            }
            return ResponseEntity.ok(pengirimanService.getPengirimanHistoryByDriver(driverId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Mandor melihat driver yang tersedia dan filter dari nama.*/
    @GetMapping("/drivers")
    public ResponseEntity<?> getDrivers(
            @RequestHeader("X-User-Id") Long mandorId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(value = "searchName", required = false) String searchName) {
        try {
            ensureRole(userRole, ROLE_MANDOR);
            List<User> drivers = pengirimanService.getAvailableDriversForMandor(mandorId, searchName);
            return ResponseEntity.ok(drivers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Mandor melihat ongoing pengiriman. */
    @GetMapping("/ongoing")
    public ResponseEntity<List<Pengiriman>> getOngoingPengiriman(
            @RequestHeader("X-User-Id") Long mandorId,
            @RequestHeader("X-User-Role") String userRole) {
        try {
            ensureRole(userRole, ROLE_MANDOR);
            return ResponseEntity.ok(pengirimanService.getOngoingPengiriman(mandorId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /* Mandor approve pengiriman setelah status TIBA_DI_TUJUAN. */
    @PatchMapping("/{pengirimanId}/mandor/approve")
    public ResponseEntity<?> approveByMandor(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long mandorId,
            @RequestHeader("X-User-Role") String userRole) {
        try {
            ensureRole(userRole, ROLE_MANDOR);
            return ResponseEntity.ok(pengirimanService.approveByMandor(pengirimanId, mandorId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Mandor reject pengiriman dengan alasan. */
    @PatchMapping("/{pengirimanId}/mandor/reject")
    public ResponseEntity<?> rejectByMandor(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long mandorId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody MandorRejectionRequest request) {
        try {
            ensureRole(userRole, ROLE_MANDOR);
            return ResponseEntity.ok(
                    pengirimanService.rejectByMandor(pengirimanId, mandorId, request.getRejectionReason()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Admin melihat pengiriman yang telah approved oleh mandor, bisa filter nama mandor dan tanggal. */
    @GetMapping("/admin/approved-mandor")
    public ResponseEntity<?> getApprovedByMandorForAdmin(
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(value = "mandorName", required = false) String mandorName,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            return ResponseEntity.ok(pengirimanService.getApprovedPengirimanForAdmin(mandorName, date));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Admin approve pengiriman yang sebelumnya approved oleh mandor. */
    @PatchMapping("/{pengirimanId}/admin/approve")
    public ResponseEntity<?> approveByAdmin(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String userRole) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            return ResponseEntity.ok(pengirimanService.approveByAdmin(pengirimanId, adminId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Admin reject pengiriman dengan alasan. */
    @PatchMapping("/{pengirimanId}/admin/reject")
    public ResponseEntity<?> rejectByAdmin(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody AdminRejectionRequest request) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            return ResponseEntity.ok(
                    pengirimanService.rejectByAdmin(pengirimanId, adminId, request.getRejectionReason()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /* Admin partial reject: sebagian berat tetap diakui, alasan wajib. */
    @PatchMapping("/{pengirimanId}/admin/partial-reject")
    public ResponseEntity<?> partialRejectByAdmin(
            @PathVariable Long pengirimanId,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody AdminPartialRejectionRequest request) {
        try {
            ensureRole(userRole, ROLE_ADMIN);
            return ResponseEntity.ok(
                    pengirimanService.partialRejectByAdmin(
                            pengirimanId,
                            adminId,
                            request.getAcknowledgedWeightKg(),
                            request.getRejectionReason()
                    )
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /** Get a specific pengiriman by ID. */
    @GetMapping("/{pengirimanId}")
    public ResponseEntity<?> getPengirimanById(@PathVariable Long pengirimanId) {
        try {
            return ResponseEntity.ok(pengirimanService.getPengirimanById(pengirimanId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
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