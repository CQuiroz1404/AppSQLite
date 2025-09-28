package com.example.appconsqlite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Menu extends AppCompatActivity {

    int numero;

    private Button btnAdd1, btnAdd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar botones
        btnAdd1 = findViewById(R.id.btnAdd1);
        btnAdd2 = findViewById(R.id.btnAdd2);

        // Acción de agregar al carrito (simulada)
        btnAdd1.setOnClickListener(v ->
                Toast.makeText(this, "Producto 1 agregado al carrito", Toast.LENGTH_SHORT).show());

        btnAdd2.setOnClickListener(v ->
                Toast.makeText(this, "Producto 2 agregado al carrito", Toast.LENGTH_SHORT).show());
    }
}
