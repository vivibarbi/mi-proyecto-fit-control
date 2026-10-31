package com.example.fitcontrol.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_peso")
public class RegistroPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double peso;

    private LocalDate fecha;

    public RegistroPeso() {
    }

    public RegistroPeso(double peso) {
        this.peso = peso;
        this.fecha = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public double getPeso() {
        return peso;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}