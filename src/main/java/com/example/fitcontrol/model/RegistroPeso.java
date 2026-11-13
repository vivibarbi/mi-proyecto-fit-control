package com.example.fitcontrol.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(
    name = "registro_peso",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"usuario_id", "fecha"}
        )
    }
)

public class RegistroPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double peso;

    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public RegistroPeso() {
    }

    public RegistroPeso(double peso, Usuario usuario, LocalDate fecha) {
        this.peso = peso;
        this.fecha = fecha;
        this.usuario = usuario;
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

    @JsonIgnore
    public Usuario getUsuario() {
        return usuario;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}