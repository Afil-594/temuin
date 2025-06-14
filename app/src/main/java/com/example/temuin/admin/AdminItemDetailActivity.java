package com.example.temuin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.temuin.R;
import com.example.temuin.found.FoundItem;
import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminItemDetailActivity extends AppCompatActivity {

    private TextView tvName, tvLocation, tvDate, tvTime, tvColor, tvDescription, tvStatus;
    private ImageView ivItemImage;
    private Button btnVerifikasi, btnTolak;
    private String itemId;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_item_detail);

        tvName = findViewById(R.id.tv_detail_name);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTime = findViewById(R.id.tv_detail_time);
        tvStatus = findViewById(R.id.tv_detail_status);
        ivItemImage = findViewById(R.id.iv_detail_image);
        btnVerifikasi = findViewById(R.id.btn_verifikasi);
        btnTolak = findViewById(R.id.btn_tolak);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId == null) {
            Toast.makeText(this, "ID item tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("found_items").child(itemId);

        setupRealtimeDataAndViewListener();

        btnVerifikasi.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Verifikasi Laporan")
                    .setMessage("Apakah Anda yakin ingin memverifikasi laporan ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "diverifikasi");
                        updates.put("verifiedTimestamp", ServerValue.TIMESTAMP);

                        databaseReference.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Laporan berhasil diverifikasi", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Gagal memverifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnTolak.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tolak Laporan")
                    .setMessage("Apakah Anda yakin ingin menolak laporan ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "ditolak");
                        updates.put("rejectedTimestamp", ServerValue.TIMESTAMP); // <-- TAMBAHKAN INI

                        databaseReference.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Laporan ditolak", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal menolak laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_barang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(AdminItemDetailActivity.this, LaporPenemuanActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(AdminItemDetailActivity.this, AdminDashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                startActivity(new Intent(AdminItemDetailActivity.this, LaporKehilanganActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupRealtimeDataAndViewListener() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdminItemDetailActivity.this, "Data tidak ditemukan atau telah diarsipkan", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                FoundItem item = snapshot.getValue(FoundItem.class);
                if (item != null) {
                    // Set data ke TextViews
                    tvName.setText(item.getName());
                    tvLocation.setText(item.getLocation());
                    tvDate.setText(item.getDate());
                    tvTime.setText(item.getTime());
                    tvStatus.setText(item.getStatus());

                    // Logika visibilitas tombol
                    String status = item.getStatus();
                    if ("belum diverifikasi".equals(status)) {
                        btnVerifikasi.setVisibility(View.VISIBLE);
                        btnTolak.setVisibility(View.VISIBLE);
                    } else {
                        // Jika statusnya bukan "belum diverifikasi", sembunyikan kedua tombol
                        btnVerifikasi.setVisibility(View.GONE);
                        btnTolak.setVisibility(View.GONE);
                    }

                    if (item.getImagePath() != null) {
                        Glide.with(AdminItemDetailActivity.this)
                                .load("file://" + item.getImagePath())
                                .into(ivItemImage);
                    }
                } else {
                    Toast.makeText(AdminItemDetailActivity.this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminItemDetailActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
