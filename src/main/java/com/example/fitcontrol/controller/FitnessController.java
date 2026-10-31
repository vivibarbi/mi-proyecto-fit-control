package com.example.fitcontrol.controller;

import com.example.fitcontrol.service.FitnessService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/fitness")
@CrossOrigin
public class FitnessController {
//Este controlador recibirá los datos enviados desde el frontend.

    private final FitnessService fitnessService;

    public FitnessController(FitnessService fitnessService) {
        this.fitnessService = fitnessService;
    }

    @GetMapping("/calcular")
    public Map<String, Object> calcular(
            @RequestParam double peso,
            @RequestParam double altura,
            @RequestParam int edad,
            @RequestParam String sexo,
            @RequestParam String actividad,
            @RequestParam String objetivo) {

        return fitnessService.calcular(
            peso, altura, edad, sexo, actividad, objetivo
        );
    }
}