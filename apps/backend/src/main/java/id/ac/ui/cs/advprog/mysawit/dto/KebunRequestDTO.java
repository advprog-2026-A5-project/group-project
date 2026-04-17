package id.ac.ui.cs.advprog.mysawit.dto;

import lombok.Data;
import java.util.List;

@Data
public class KebunRequestDTO {
    private String nama;
    private String kodeKebun;
    private Double luas;
    private List<CoordinateDTO> koordinat;
}