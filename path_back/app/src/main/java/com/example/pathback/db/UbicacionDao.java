package com.example.pathback.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UbicacionDao {
    @Insert
    void insertar(Ubicacion ubicacion);
    @Query("SELECT * FROM Ubicacion ORDER BY id DESC")
    List<Ubicacion> obtenerTodas();
}

