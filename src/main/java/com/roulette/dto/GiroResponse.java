package com.roulette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiroResponse {
    private String tipo = "GIRO";
    private List<String> nombres;
    private int targetIndex;
    private String targetName;
    private double finalRotation;  // ángulo total en grados
    private long duration;  // milisegundos
    private long timestamp;  // para sincronización
}