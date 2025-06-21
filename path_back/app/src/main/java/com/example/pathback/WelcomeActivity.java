package com.example.pathback;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private Button btnLearnMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar botones
        btnGetStarted = findViewById(R.id.btn_get_started);
        btnLearnMore = findViewById(R.id.btn_learn_more);

        // Configurar listeners
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnLearnMore.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, InfoActivity.class);
            startActivity(intent);
        });
    }
}