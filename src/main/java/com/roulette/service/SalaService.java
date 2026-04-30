package com.roulette.service;

import com.roulette.dto.*;
import com.roulette.model.EstadoSala;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SalaService {

    // Almacena las salas en memoria
    private final Map<String, EstadoSala> salas = new ConcurrentHashMap<>();

    // Para enviar mensajes a los clientes WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    // Para generar números aleatorios
    private final Random random = new Random();

    // Constructor - Spring inyecta SimpMessagingTemplate automáticamente
    public SalaService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Obtiene una sala, si no existe la crea
     */
    public EstadoSala obtenerSala(String salaId) {
        return salas.computeIfAbsent(salaId, EstadoSala::new);
    }

    /**
     * Establece la lista de nombres en una sala
     */
    public void establecerNombres(String salaId, List<String> nombres) {
        EstadoSala sala = obtenerSala(salaId);

        // Limpiar nombres vacíos
        List<String> nombresLimpios = nombres.stream()
                .filter(n -> n != null && !n.trim().isEmpty())
                .map(String::trim)
                .toList();

        sala.setNombres(new ArrayList<>(nombresLimpios));
        sala.actualizarActividad();

        // Si el target ya no es válido, resetearlo
        if (!sala.isTargetValido()) {
            sala.setTargetIndex(null);
        }

        // Notificar a todas las vistas públicas
        notificarCambioNombres(salaId);

        // Notificar a la vista de control
        notificarEstadoControl(salaId);
    }

    /**
     * Elimina un nombre por índice
     */
    public void eliminarNombre(String salaId, int index) {
        EstadoSala sala = obtenerSala(salaId);

        if (index >= 0 && index < sala.getNombres().size()) {
            sala.getNombres().remove(index);
            sala.actualizarActividad();

            // Ajustar el target si es necesario
            if (sala.getTargetIndex() != null) {
                if (sala.getTargetIndex() == index) {
                    // Si eliminamos el target, lo reseteamos
                    sala.setTargetIndex(null);
                } else if (sala.getTargetIndex() > index) {
                    // Si eliminamos uno antes, ajustamos el índice
                    sala.setTargetIndex(sala.getTargetIndex() - 1);
                }
            }

            notificarCambioNombres(salaId);
            notificarEstadoControl(salaId);
        }
    }

    /**
     * Establece el target desde la vista de control
     */
    public void establecerTarget(String salaId, Integer targetIndex) {
        EstadoSala sala = obtenerSala(salaId);
        sala.setTargetIndex(targetIndex);
        sala.actualizarActividad();

        // Solo notificar a la vista de control
        notificarEstadoControl(salaId);
    }

    /**
     * AQUÍ ESTÁ LA MAGIA - Gira la ruleta
     */
    public void girarRuleta(String salaId) {
        EstadoSala sala = obtenerSala(salaId);

        // Validaciones
        if (sala.isGirando()) {
            System.out.println("⚠️ La sala ya está girando");
            return;
        }

        if (sala.getNombres().isEmpty()) {
            System.out.println("⚠️ No hay nombres en la sala");
            return;
        }

        sala.setGirando(true);
        sala.actualizarActividad();

        // ============================================
        // AQUÍ ESTÁ EL TRUCO
        // ============================================

        int targetIndex;

        // Si hay un target forzado, usarlo
        if (sala.isTargetValido()) {
            targetIndex = sala.getTargetIndex();
            System.out.println("🎯 GIRO FORZADO → índice " + targetIndex);
        } else {
            // Si no, aleatorio
            targetIndex = random.nextInt(sala.getNombres().size());
            System.out.println("🎲 Giro aleatorio → índice " + targetIndex);
        }

        String targetName = sala.getNombres().get(targetIndex);

        // ============================================
        // CÁLCULO DEL ÁNGULO
        // ============================================

        // Ángulo de cada segmento
        double segmentAngle = 360.0 / sala.getNombres().size();

        // Centro del segmento objetivo
        double targetCenter = targetIndex * segmentAngle + segmentAngle / 2.0;

        // Rotación necesaria para que el centro quede arriba
        double baseRotation = (360.0 - targetCenter + 360.0) % 360.0;

        // Agregar 6 vueltas completas para que se vea bien
        double totalRotation = 360.0 * 6 + baseRotation;

        long duration = 5500;  // 5.5 segundos

        // ============================================

        // Crear la respuesta
        GiroResponse response = new GiroResponse(
                "GIRO",
                sala.getNombres(),
                targetIndex,
                targetName,
                totalRotation,
                duration,
                System.currentTimeMillis()
        );

        // Agregar al historial
        if (!sala.getHistorial().contains(targetName)) {
            sala.getHistorial().add(targetName);
        }

        // Consumir el target (se usa solo una vez)
        sala.setTargetIndex(null);

        // Enviar a TODAS las vistas públicas
        messagingTemplate.convertAndSend(
                "/topic/sala/" + salaId + "/publico",
                response
        );

        System.out.println("✅ Giro enviado: " + targetName + " (rotación: " + totalRotation + "°)");

        // Después de la duración, marcar como no girando
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sala.setGirando(false);
                notificarEstadoControl(salaId);
            }
        }, duration);

        // Actualizar control
        notificarEstadoControl(salaId);
    }

    /**
     * Notifica cambios en los nombres a las vistas públicas
     */
    private void notificarCambioNombres(String salaId) {
        EstadoSala sala = obtenerSala(salaId);
        NombresUpdateResponse response = new NombresUpdateResponse(
                "NOMBRES_UPDATE",
                sala.getNombres()
        );
        messagingTemplate.convertAndSend(
                "/topic/sala/" + salaId + "/publico",
                response
        );
    }

    /**
     * Notifica el estado completo a la vista de control
     */
    private void notificarEstadoControl(String salaId) {
        EstadoSala sala = obtenerSala(salaId);
        ControlStateResponse response = new ControlStateResponse(
                "CONTROL_STATE",
                sala.getNombres(),
                sala.getTargetIndex(),
                sala.getHistorial()
        );
        messagingTemplate.convertAndSend(
                "/topic/sala/" + salaId + "/control",
                response
        );
    }
}
