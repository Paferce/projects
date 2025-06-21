package com.example.pathback.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ubicacion.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UbicacionDao ubicacionDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "ubicaciones_db"
            ).build();
        }
        return INSTANCE;
    }
}

