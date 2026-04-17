package id.ac.ui.cs.advprog.mysawit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPartialRejectionRequest {
    private Double acknowledgedWeightKg;
    private String rejectionReason;
}
