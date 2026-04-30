package com.roulette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlStateResponse {
    private String tipo = "CONTROL_STATE";
    private List<String> nombres;
    private Integer targetIndex;
    private List<String> historial;
}
