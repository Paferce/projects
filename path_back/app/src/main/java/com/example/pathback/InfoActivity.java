package com.example.pathback;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    private Button btnContacto;
    private Button btnVolver;

    // Aquí puedes cambiar el email por el tuyo
    private static final String EMAIL_CONTACTO = "contacto@pathback.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Inicializar botones
        btnContacto = findViewById(R.id.btn_contacto);
        btnVolver = findViewById(R.id.btn_volver);

        // Configurar listener para el botón de contacto
        btnContacto.setOnClickListener(v -> {
            enviarEmail();
        });

        // Configurar listener para el botón de volver
        btnVolver.setOnClickListener(v -> {
            finish(); // Cierra esta actividad y vuelve a la anterior
        });
    }

    private void enviarEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + EMAIL_CONTACTO));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre PathBack");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola,\n\nMe gustaría obtener más información sobre PathBack.\n\nSaludos.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No hay aplicaciones de email instaladas", Toast.LENGTH_SHORT).show();
        }
    }
}