package com.roulette.controller;

import com.roulette.dto.*;
import com.roulette.service.SalaService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class RuletaWebSocketController {

    private final SalaService salaService;

    // Constructor - Spring inyecta SalaService automáticamente
    public RuletaWebSocketController(SalaService salaService) {
        this.salaService = salaService;
    }

    /**
     * Recibe la lista de nombres desde el profesor
     * Cliente envía a: /app/sala/agregar-nombres
     */
    @MessageMapping("/sala/agregar-nombres")
    public void agregarNombres(@Payload AgregarNombresRequest request) {
        System.out.println("📨 Recibido: agregar nombres en sala " + request.getSalaId());
        salaService.establecerNombres(request.getSalaId(), request.getNombres());
    }

    /**
     * Elimina un nombre por índice
     * Cliente envía a: /app/sala/eliminar-nombre
     */
    @MessageMapping("/sala/eliminar-nombre")
    public void eliminarNombre(@Payload EliminarNombreRequest request) {
        System.out.println("📨 Recibido: eliminar nombre " + request.getIndex() + " en sala " + request.getSalaId());
        salaService.eliminarNombre(request.getSalaId(), request.getIndex());
    }

    /**
     * Establece el target desde la vista de control
     * Cliente envía a: /app/sala/set-target
     */
    @MessageMapping("/sala/set-target")
    public void setTarget(@Payload SetTargetRequest request) {
        System.out.println("📨 Recibido: set target " + request.getTargetIndex() + " en sala " + request.getSalaId());
        salaService.establecerTarget(request.getSalaId(), request.getTargetIndex());
    }

    /**
     * Gira la ruleta
     * Cliente envía a: /app/sala/girar
     */
    @MessageMapping("/sala/girar")
    public void girarRuleta(@Payload GirarRuletaRequest request) {
        System.out.println("📨 Recibido: girar ruleta en sala " + request.getSalaId());
        salaService.girarRuleta(request.getSalaId());
    }
}