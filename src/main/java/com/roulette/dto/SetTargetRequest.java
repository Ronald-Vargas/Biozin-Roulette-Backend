package com.roulette.dto;

import lombok.Data;

@Data
public class SetTargetRequest {
    private String salaId;
    private Integer targetIndex;  // null = aleatorio
}