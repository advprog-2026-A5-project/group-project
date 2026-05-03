package id.ac.ui.cs.advprog.mysawit.model;

import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "upah")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Upah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private UpahRole role;

    @Column(nullable = false)
    private double upahPerKg;

}
