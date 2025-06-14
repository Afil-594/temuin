package com.example.temuin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PanduanActivity extends AppCompatActivity {
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panduan);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(PanduanActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(PanduanActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                Intent intent = new Intent(PanduanActivity.this, LostItemsListActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

    }
}