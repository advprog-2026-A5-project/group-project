package id.ac.ui.cs.advprog.mysawit.dto;

import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import lombok.Data;

@Data
public class UpahRequestDTO {
    private UpahRole role;
    private Double upahPerKg;
}
