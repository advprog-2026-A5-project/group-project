package id.ac.ui.cs.advprog.mysawit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pengiriman")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pengiriman {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne
    @JoinColumn(name = "mandor_id", nullable = false)
    private User mandor;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPengiriman status;

    @Column(nullable = false)
    private double totalWeightKg;

    private String rejectionReason;
    private Double acknowledgedWeightKg;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusPengiriman.MEMUAT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}