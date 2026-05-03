package id.ac.ui.cs.advprog.mysawit.dto;

import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpahResponseDTO {
    private UpahRole role;
    private double upahPerKg;
}
