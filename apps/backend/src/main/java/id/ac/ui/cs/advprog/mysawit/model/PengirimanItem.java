package id.ac.ui.cs.advprog.mysawit.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "shipment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PengirimanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Pengiriman shipment;

    // Reference to approved harvest ID from another module
    @Column(nullable = false)
    private UUID harvestId;

    @Column(nullable = false)
    private double weightKg;
}