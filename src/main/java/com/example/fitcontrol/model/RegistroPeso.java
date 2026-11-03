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

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "usuario_id", nullable = false)
private Usuario usuario;

public RegistroPeso(double peso, Usuario usuario) {
    this.peso = peso;
    this.fecha = LocalDate.now();
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
