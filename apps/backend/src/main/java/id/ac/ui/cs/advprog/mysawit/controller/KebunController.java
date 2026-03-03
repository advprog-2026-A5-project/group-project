package id.ac.ui.cs.advprog.mysawit.controller;

import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.service.KebunService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kebun")
public class KebunController {

    private final KebunService kebunService;

    public KebunController(KebunService kebunService) {
        this.kebunService = kebunService;
    }

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
    public ResponseEntity<List<Kebun>> getAll() {
        return ResponseEntity.ok(kebunService.findAll());
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
            return ResponseEntity.notFound().build();
        }
    }
}