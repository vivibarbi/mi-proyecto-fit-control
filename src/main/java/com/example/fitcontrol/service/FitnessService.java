package com.example.fitcontrol.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FitnessService {
//*calculadora fitness */

    public Map<String, Object> calcular(
            double peso,
            double altura,
            int edad,
            String sexo,
            String actividad,
            String objetivo) {

        if (peso <= 0 || altura <= 0 || edad < 18) {
            throw new IllegalArgumentException(
                "Datos inválidos. La aplicación está diseñada para adultos."
            );
        }

        double tmb;
        
    /* formulas para calcular peso */
    
        if (sexo.equalsIgnoreCase("M")) {
            tmb = (10 * peso) + (6.25 * altura)
                    - (5 * edad) + 5;
        } else if (sexo.equalsIgnoreCase("F")) {
            tmb = (10 * peso) + (6.25 * altura)
                    - (5 * edad) - 161;
        } else {
            throw new IllegalArgumentException("Sexo inválido");
        }

        double factor;

        switch (actividad.toUpperCase()) {
            case "SEDENTARIO":
                factor = 1.2;
                break;
            case "LIGERO":
                factor = 1.375;
                break;
            case "MODERADO":
                factor = 1.55;
                break;
            case "ALTO":
                factor = 1.725;
                break;
            case "MUY_ALTO":
                factor = 1.9;
                break;
            default:
                throw new IllegalArgumentException(
                    "Nivel de actividad inválido"
                );
        }

        double tdee = tmb * factor;

        double calorias;

        switch (objetivo.toUpperCase()) {
            case "PERDER":
                calorias = tdee * 0.85;
                break;
            case "GANAR":
                calorias = tdee * 1.10;
                break;
            case "MANTENER":
                calorias = tdee;
                break;
            default:
                throw new IllegalArgumentException(
                    "Objetivo inválido"
                );
        }

        // Distribución de macronutrientes
        double proteinas = peso * 1.8;
        double grasas = peso * 0.8;

        double caloriasProteinas = proteinas * 4;
        double caloriasGrasas = grasas * 9;

        double carbohidratos =
                (calorias - caloriasProteinas - caloriasGrasas) / 4;

        double imc = peso / Math.pow(altura / 100, 2);

        return Map.of(
            "tmb", Math.round(tmb),
            "tdee", Math.round(tdee),
            "caloriasObjetivo", Math.round(calorias),
            "proteinas", Math.round(proteinas),
            "grasas", Math.round(grasas),
            "carbohidratos", Math.round(carbohidratos),
            "imc", Math.round(imc * 10.0) / 10.0
        );
    }
}