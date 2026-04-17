package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.AssignRequestDTO;
import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
import id.ac.ui.cs.advprog.mysawit.dto.ReassignRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.service.KebunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kebun")
public class KebunController {

    @Autowired
    private KebunService kebunService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody KebunRequestDTO request) {
        try {
            Kebun kebun = kebunService.create(request);
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Kebun>> getAll(
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String kodeKebun) {
        return ResponseEntity.ok(kebunService.findAll(nama, kodeKebun));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(kebunService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody KebunRequestDTO request) {
        try {
            Kebun kebun = kebunService.update(id, request);
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            kebunService.delete(id);
            return ResponseEntity.ok("Kebun berhasil dihapus");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ASSIGN / UNASSIGN ENDPOINTS ==========

    @PutMapping("/{id}/assign-mandor")
    public ResponseEntity<?> assignMandor(@PathVariable Long id, @RequestBody AssignRequestDTO request) {
        try {
            Kebun kebun = kebunService.assignMandor(id, request.getName());
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/assign-supir")
    public ResponseEntity<?> assignSupir(@PathVariable Long id, @RequestBody AssignRequestDTO request) {
        try {
            Kebun kebun = kebunService.assignSupir(id, request.getName());
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/unassign-mandor")
    public ResponseEntity<?> unassignMandor(@PathVariable Long id, @RequestBody ReassignRequestDTO request) {
        try {
            Kebun kebun = kebunService.unassignMandor(id, request.getTargetKebunId());
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/unassign-supir")
    public ResponseEntity<?> unassignSupir(@PathVariable Long id, @RequestBody ReassignRequestDTO request) {
        try {
            Kebun kebun = kebunService.unassignSupir(id, request.getSupirName(), request.getTargetKebunId());
            return ResponseEntity.ok(kebun);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}