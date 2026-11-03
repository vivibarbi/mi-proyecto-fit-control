package com.example.fitcontrol.model;

import jakarta.persistence.*;
  
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String nombre;

    private String password;

    //Proveedor	Significado
//LOCAL	Se registró con correo y contraseña.
//GOOGLE	Inició sesión con su cuenta de Google.
    private String proveedor;

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPassword() {
        return password;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}


