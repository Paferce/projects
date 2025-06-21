package com.example.pathback;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MarcadorDao {

    @Insert
    void insertar(Marcador marcador);

    @Query("SELECT * FROM Marcador")
    List<Marcador> obtenerTodos();

    @Delete
    void eliminar(Marcador marcador);

    @Query("DELETE FROM Marcador")
    void eliminarTodos();
}