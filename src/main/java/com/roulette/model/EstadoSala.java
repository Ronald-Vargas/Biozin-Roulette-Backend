package com.roulette.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EstadoSala {

    private String salaId;
    private List<String> nombres;
    private Integer targetIndex;  // null = aleatorio, número = forzado
    private List<String> historial;
    private LocalDateTime ultimaActividad;
    private boolean girando;

    public EstadoSala(String salaId) {
        this.salaId = salaId;
        this.nombres = new ArrayList<>();
        this.historial = new ArrayList<>();
        this.targetIndex = null;
        this.ultimaActividad = LocalDateTime.now();
        this.girando = false;
    }

    public void actualizarActividad() {
        this.ultimaActividad = LocalDateTime.now();
    }

    public boolean isTargetValido() {
        return targetIndex != null
                && targetIndex >= 0
                && targetIndex < nombres.size();
    }
}