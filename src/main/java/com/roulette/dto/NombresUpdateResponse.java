package com.roulette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NombresUpdateResponse {
    private String tipo = "NOMBRES_UPDATE";
    private List<String> nombres;
}
