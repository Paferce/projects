package com.example.pathback;

import androidx.room.Entity; // Tabla en la base de datos
import androidx.room.PrimaryKey; // ID = Clave Primaria

@Entity
public class Marcador {
    @PrimaryKey(autoGenerate = true) // El ID se genera automáticamente
    public int id;

    public String nombre; // Nombre del marcador

    // Coordenadas del marcador
    public double latitud;
    public double longitud;
}
