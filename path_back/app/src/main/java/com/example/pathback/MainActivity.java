package com.example.pathback;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Polyline;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.os.Handler;
import android.os.Looper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double DEFAULT_ZOOM = 18.0;

    private MapView mapa;
    private Button btnGuardar;
    private Button btnCentrar;
    private LocationManager locationManager;
    private GeoPoint currentLocation;
    private Marker userMarker;
    private Marker lastSavedMarker;
    private boolean isNavigating = false;
    private Polyline routeLine;
    private boolean isRoutingActive = false;
    private Button btnCentrarMarcador;
    private Button btnEliminarMarcador;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.mapa);

        // Configuración inicial de OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Inicializar base de datos
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "marcadores-db")
                .build();

        // Inicializar vistas
        mapa = findViewById(R.id.mapa);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCentrar = findViewById(R.id.btnCentrar);
        btnCentrarMarcador = findViewById(R.id.btnCentrarMarcador);
        btnEliminarMarcador = findViewById(R.id.btnEliminarMarcador);

        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // Cargar el último marcador guardado (solo uno)
        cargarUltimoMarcador();

        // Ocultos por defecto
        btnCentrarMarcador.setVisibility(View.GONE);
        btnEliminarMarcador.setVisibility(View.GONE);

        // Configurar el mapa
        mapa.setMultiTouchControls(true);
        mapa.setMinZoomLevel(3.0);
        mapa.setMaxZoomLevel(19.0);
        mapa.getController().setZoom(DEFAULT_ZOOM);

        // Inicializar el marcador del usuario
        userMarker = new Marker(mapa);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        userMarker.setTitle("Tu ubicación");

        // Personalizar el icono del marcador del usuario
        Drawable circleIcon = ContextCompat.getDrawable(this, R.drawable.ic_user_location);
        if (circleIcon != null) {
            userMarker.setIcon(circleIcon);
        }

        mapa.getOverlays().add(userMarker);

        // Inicializar el LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Inicializar routeLine
        routeLine = new Polyline();
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(5.0f);
        mapa.getOverlays().add(routeLine);

        // Configurar botones
        btnGuardar.setOnClickListener(v -> {
            if (!isNavigating) {
                if (currentLocation != null) {
                    guardarUbicacion(currentLocation);
                } else {
                    Toast.makeText(this, "Esperando ubicación GPS", Toast.LENGTH_SHORT).show();
                }
            } else {
                btnGuardar.setVisibility(View.GONE);
                btnCentrarMarcador.setVisibility(View.VISIBLE);
                btnEliminarMarcador.setVisibility(View.VISIBLE);

                if (currentLocation != null && lastSavedMarker != null) {
                    getRouteFromOSRM(currentLocation, lastSavedMarker.getPosition());
                    isRoutingActive = true;
                } else {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEliminarMarcador.setOnClickListener(v -> {
            eliminarMarcador();
        });

        btnCentrarMarcador.setOnClickListener(v -> {
            if (lastSavedMarker != null) {
                mapa.getController().animateTo(lastSavedMarker.getPosition());
            }
        });

        // Añadir el OnClickListener para el botón de centrar
        btnCentrar.setOnClickListener(v -> {
            if (currentLocation != null) {
                mapa.getController().animateTo(currentLocation);
            }
        });

        // Iniciar las actualizaciones de ubicación
        checkLocationPermission();
    }

    private void cargarUltimoMarcador() {
        new Thread(() -> {
            List<Marcador> marcadores = database.marcadorDao().obtenerTodos();

            runOnUiThread(() -> {
                // Solo mostrar el último marcador si existe
                if (!marcadores.isEmpty()) {
                    Marcador ultimo = marcadores.get(marcadores.size() - 1);

                    lastSavedMarker = new Marker(mapa);
                    lastSavedMarker.setPosition(new GeoPoint(ultimo.latitud, ultimo.longitud));
                    lastSavedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    lastSavedMarker.setTitle("Ubicación guardada");

                    Drawable markerIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation);
                    if (markerIcon != null) {
                        lastSavedMarker.setIcon(markerIcon);
                    }

                    mapa.getOverlays().add(lastSavedMarker);
                    isNavigating = true;
                    btnGuardar.setText("Volver al marcador");
                }
                mapa.invalidate();
            });
        }).start();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        userMarker.setPosition(currentLocation);

        // Centrar solo la primera vez
        if (currentLocation != null && !userMarker.isEnabled()) {
            mapa.getController().setZoom(DEFAULT_ZOOM);
            mapa.getController().animateTo(currentLocation);
            userMarker.setEnabled(true);
        }

        // Actualizar la ruta en tiempo real si se está navegando
        if (isNavigating && isRoutingActive && lastSavedMarker != null) {
            getRouteFromOSRM(currentLocation, lastSavedMarker.getPosition());
        }

        mapa.invalidate();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Intentar obtener última ubicación conocida
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastLocation != null) {
                currentLocation = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                userMarker.setPosition(currentLocation);
                mapa.getController().setZoom(DEFAULT_ZOOM);
                mapa.getController().setCenter(currentLocation);
                userMarker.setEnabled(true);
            }

            // Comenzar actualizaciones de ubicación
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    this
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Se requieren permisos de ubicación", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void guardarUbicacion(GeoPoint ubicacion) {
        // Eliminar marcador anterior del mapa si existe
        if (lastSavedMarker != null) {
            mapa.getOverlays().remove(lastSavedMarker);
        }

        // Crear nuevo marcador
        lastSavedMarker = new Marker(mapa);
        lastSavedMarker.setPosition(ubicacion);
        lastSavedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        lastSavedMarker.setTitle("Ubicación guardada");

        Drawable markerIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation);
        if (markerIcon != null) {
            lastSavedMarker.setIcon(markerIcon);
        }

        mapa.getOverlays().add(lastSavedMarker);
        isNavigating = true;
        btnGuardar.setText("Volver al marcador");
        mapa.invalidate();

        // Guardar en la base de datos (eliminar todos los anteriores y guardar solo este)
        new Thread(() -> {
            // Eliminar todos los marcadores existentes
            List<Marcador> todosLosMarcadores = database.marcadorDao().obtenerTodos();
            for (Marcador m : todosLosMarcadores) {
                database.marcadorDao().eliminar(m);
            }

            // Insertar el nuevo marcador
            Marcador nuevoMarcador = new Marcador();
            nuevoMarcador.nombre = "Ubicación guardada";
            nuevoMarcador.latitud = ubicacion.getLatitude();
            nuevoMarcador.longitud = ubicacion.getLongitude();

            database.marcadorDao().insertar(nuevoMarcador);
        }).start();
    }

    private void eliminarMarcador() {
        if (lastSavedMarker != null) {
            // Remover el marcador del mapa
            mapa.getOverlays().remove(lastSavedMarker);
            lastSavedMarker = null;

            // Limpiar la ruta si existe
            if (routeLine != null) {
                routeLine.setPoints(new ArrayList<>());
            }

            // Restaurar el estado inicial
            isNavigating = false;
            isRoutingActive = false;

            // Restaurar la visibilidad de los botones
            btnGuardar.setText("Guardar ubicación");
            btnGuardar.setVisibility(View.VISIBLE);
            btnCentrarMarcador.setVisibility(View.GONE);
            btnEliminarMarcador.setVisibility(View.GONE);

            // Eliminar de la base de datos
            new Thread(() -> {
                List<Marcador> todosLosMarcadores = database.marcadorDao().obtenerTodos();
                for (Marcador m : todosLosMarcadores) {
                    database.marcadorDao().eliminar(m);
                }
            }).start();

            // Refrescar el mapa
            mapa.invalidate();

            Toast.makeText(this, "Marcador eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No hay marcador para eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRouteFromOSRM(GeoPoint start, GeoPoint end) {
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + start.getLongitude() + "," + start.getLatitude() + ";"
                + end.getLongitude() + "," + end.getLatitude()
                + "?overview=full&geometries=geojson";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    ArrayList<GeoPoint> routePoints = parseRouteFromJson(responseBody);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        routeLine.setPoints(routePoints);
                        mapa.invalidate();
                    });
                }
            }
        });
    }

    private ArrayList<GeoPoint> parseRouteFromJson(String json) {
        ArrayList<GeoPoint> routePoints = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray coords = obj.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates");

            for (int i = 0; i < coords.length(); i++) {
                JSONArray point = coords.getJSONArray(i);
                double lon = point.getDouble(0);
                double lat = point.getDouble(1);
                routePoints.add(new GeoPoint(lat, lon));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routePoints;
    }
}