package com.example.pathback;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Indica qué tablas contiene y la versión de la base de datos
@Database(entities = {Marcador.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // Permite acceder a las funciones del DAO
    public abstract MarcadorDao marcadorDao();
}
