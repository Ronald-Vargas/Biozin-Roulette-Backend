package com.roulette.dto;

import lombok.Data;
import java.util.List;

@Data
public class AgregarNombresRequest {
    private String salaId;
    private List<String> nombres;
}