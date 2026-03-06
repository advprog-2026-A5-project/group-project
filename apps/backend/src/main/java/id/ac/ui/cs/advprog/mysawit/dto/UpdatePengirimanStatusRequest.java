package id.ac.ui.cs.advprog.mysawit.dto;

import id.ac.ui.cs.advprog.mysawit.model.PengirimanStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePengirimanStatusRequest {
    private PengirimanStatus newStatus;
}