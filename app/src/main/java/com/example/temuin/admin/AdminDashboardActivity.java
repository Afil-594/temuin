package com.example.temuin.admin;

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

import com.example.temuin.LoginActivity;
import com.example.temuin.R;
import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.found.ReportFoundItemActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.example.temuin.lost.ReportLostItemActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class AdminDashboardActivity extends AppCompatActivity {
    private ImageView btnProfile;
    private LinearLayout profileOverlay;
    private Button btnLogout;
    private TextView tvNama, tvJurusan, tvEmail, tvWelcome;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private LinearLayout cardDaftarBarang, cardDaftarBarangHilang, cardLaporPenemuan, cardLaporKehilangan;

    private String currentRole = "user"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

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
        cardLaporPenemuan = findViewById(R.id.cardLaporPenemuan);
        cardLaporKehilangan = findViewById(R.id.cardLaporKehilangan);

        // Ambil data user dari Realtime DB
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (!"admin".equals(role)) {
                        Toast.makeText(AdminDashboardActivity.this, "Akses hanya untuk admin", Toast.LENGTH_SHORT).show();
                        finish(); // keluar dari halaman
                    }
                    else {
                        Toast.makeText(AdminDashboardActivity.this, "Selamat datang, Admin", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AdminDashboardActivity.this, "Gagal cek role", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        btnProfile.setOnClickListener(v -> {
            profileOverlay.setVisibility(profileOverlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });

        cardDaftarBarang.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, FoundItemsListActivity.class)));
        cardDaftarBarangHilang.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, LostItemsListActivity.class)));
        cardLaporPenemuan.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, LaporPenemuanActivity.class)));
        cardLaporKehilangan.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, LaporKehilanganActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(AdminDashboardActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_laporan) {
                startActivity(new Intent(AdminDashboardActivity.this, LostItemsListActivity.class));
                finish();
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
