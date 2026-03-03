package id.ac.ui.cs.advprog.mysawit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kebun")
public class Kebun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    // example : POLYGON((106 -6, 107 -6, 107 -7, 106 -7, 106 -6))
    @Column(columnDefinition = "TEXT", nullable = false)
    private String wktGeometry;
}