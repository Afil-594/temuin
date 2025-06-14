package com.example.temuin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.found.ReportFoundItemActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.example.temuin.lost.ReportLostItemActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class HomeActivity extends AppCompatActivity {
    private ImageView btnProfile;
    private LinearLayout profileOverlay;
    private Button btnLogout;
    private TextView tvNama, tvJurusan, tvEmail, tvWelcome;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private LinearLayout cardDaftarBarang, cardDaftarBarangHilang, cardPanduan;

    private String currentRole = "user"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        btnProfile = findViewById(R.id.btnProfile);
        profileOverlay = findViewById(R.id.profileOverlay);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.textWelcome);
        tvNama = findViewById(R.id.tvNama);
        tvJurusan = findViewById(R.id.tvJurusan);
        tvEmail = findViewById(R.id.tvEmail);
        cardDaftarBarang = findViewById(R.id.cardDaftarBarang);
        cardDaftarBarangHilang = findViewById(R.id.cardDaftarBarangHilang);
        cardPanduan = findViewById(R.id.cardPanduan);

        // Ambil data user dari Realtime DB
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nama = snapshot.child("nama").getValue(String.class);
                        String jurusan = snapshot.child("jurusan").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        currentRole = snapshot.child("role").getValue(String.class);

                        tvWelcome.setText("Selamat Datang, " + (nama != null ? nama : "Pengguna"));
                        tvNama.setText(nama != null ? nama : "-");
                        tvJurusan.setText(jurusan != null ? jurusan : "-");
                        tvEmail.setText(email != null ? email : "-");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnProfile.setOnClickListener(v -> {
            profileOverlay.setVisibility(profileOverlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        cardDaftarBarang.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, FoundItemsListActivity.class)));
        cardDaftarBarangHilang.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LostItemsListActivity.class)));
        cardPanduan.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, PanduanActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(HomeActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_hilang) {
                startActivity(new Intent(HomeActivity.this, LostItemsListActivity.class));
                finish();
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
