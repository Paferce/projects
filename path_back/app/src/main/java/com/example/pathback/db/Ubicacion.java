package com.example.pathback.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Ubicacion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double latitud;
    public double longitud;

    public Ubicacion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
}

