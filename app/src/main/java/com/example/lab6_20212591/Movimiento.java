package com.example.lab6_20212591;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Movimiento {
    @Exclude
    private String id;
    private String userId;
    private String tipoTarjeta; // Linea1 o LimaPass
    private Date fechaMovimiento;
    private String estacionEntrada;
    private String estacionSalida;
    private long tiempoViajeMillis;

    @ServerTimestamp
    private Date timestamp;

    public Movimiento() {
    }

    // Constructor para Línea 1
    public Movimiento(String userId, String idTarjeta, Date fechaMovimiento, String estacionEntrada, String estacionSalida, long tiempoViajeMillis) {
        this.userId = userId;
        this.id = null;
        this.tipoTarjeta = "Linea1";
        this.fechaMovimiento = fechaMovimiento;
        this.estacionEntrada = estacionEntrada;
        this.estacionSalida = estacionSalida;
        this.tiempoViajeMillis = tiempoViajeMillis;
    }

    // Constructor para Lima Pass
    public Movimiento(String userId, String idTarjeta, Date fechaMovimiento, String paraderoEntrada, String paraderoSalida) {
        this.userId = userId;
        this.id = null;
        this.tipoTarjeta = "LimaPass";
        this.fechaMovimiento = fechaMovimiento;
        this.estacionEntrada = paraderoEntrada;
        this.estacionSalida = paraderoSalida;
        this.tiempoViajeMillis = 0;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getEstacionEntrada() {
        return estacionEntrada;
    }

    public void setEstacionEntrada(String estacionEntrada) {
        this.estacionEntrada = estacionEntrada;
    }

    public String getEstacionSalida() {
        return estacionSalida;
    }

    public void setEstacionSalida(String estacionSalida) {
        this.estacionSalida = estacionSalida;
    }

    public long getTiempoViajeMillis() {
        return tiempoViajeMillis;
    }

    public void setTiempoViajeMillis(long tiempoViajeMillis) {
        this.tiempoViajeMillis = tiempoViajeMillis;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
