package id.ac.ui.cs.advprog.mysawit.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDriverRequest {
    private UUID driverId;
    private List<HarvestItemDto> harvestItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HarvestItemDto {
        private UUID harvestId;
        private double weightKg;
    }
}