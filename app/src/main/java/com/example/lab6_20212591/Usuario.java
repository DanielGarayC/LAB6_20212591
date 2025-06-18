package com.example.lab6_20212591;

public class Usuario {
    private String uid;
    private String email;
    private String nombre;
    private String apellidos;
    private String dni;

    // Constructor vacío requerido por Firestore
    public Usuario() {
    }

    public Usuario(String uid, String email, String nombre, String apellidos, String dni) {
        this.uid = uid;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
    }

    // Getters y Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}