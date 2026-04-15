package id.ac.ui.cs.advprog.mysawit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

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
    
    @Column(nullable = false, unique = true, updatable = false)
    private String kodeKebun;

    @Column(nullable = false)
    private Double luas;

    // example : POLYGON((106 -6, 107 -6, 107 -7, 106 -7, 106 -6))
    @Column(columnDefinition = "TEXT", nullable = false)
    private String wktGeometry;

    @Column
    private String mandorName;

    @ElementCollection
    @CollectionTable(name = "kebun_supir", joinColumns = @JoinColumn(name = "kebun_id"))
    @Column(name = "supir_name")
    private List<String> supirNames = new ArrayList<>();
}