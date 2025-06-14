package com.example.temuin.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.temuin.R;
import com.example.temuin.lost.LostItem;
import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.example.temuin.HomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdminItemDetailLostActivity extends AppCompatActivity {

    private TextView tvName, tvNoHp, tvNamaBarang, tvLocation, tvDate, tvStatus;
    private ImageView ivItemImage;
    private Button btnVerifikasi, btnTolak;
    private DatabaseReference databaseReference;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan nama layout sudah benar sesuai dengan file XML-mu
        setContentView(R.layout.activity_admin_item_detail_lost);

        // Inisialisasi view
        tvName = findViewById(R.id.tv_detail_name);
        tvNoHp = findViewById(R.id.tv_detail_no_hp);
        tvNamaBarang = findViewById(R.id.tv_detail_nama_barang);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDate = findViewById(R.id.tv_detail_date);
        tvStatus = findViewById(R.id.tv_detail_status);
        ivItemImage = findViewById(R.id.iv_detail_image);
        btnVerifikasi = findViewById(R.id.btn_verifikasi);
        btnTolak = findViewById(R.id.btn_tolak);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Ambil item ID dari intent
        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "ID barang tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items").child(itemId);

        // Ambil data dari database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdminItemDetailLostActivity.this, "Data barang mungkin telah dihapus atau diarsipkan.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                LostItem item = snapshot.getValue(LostItem.class);
                if (item != null) {
                    tvName.setText(item.getName());
                    tvNoHp.setText(item.getNoHp());
                    tvNamaBarang.setText(item.getNamaBarang());
                    tvLocation.setText(item.getLocation());
                    tvDate.setText(item.getDate());
                    tvStatus.setText(item.getStatus());

                    String status = item.getStatus();
                    if ("belum diverifikasi".equals(status)) {
                        btnVerifikasi.setVisibility(View.VISIBLE);
                        btnTolak.setVisibility(View.VISIBLE);
                    } else {
                        btnVerifikasi.setVisibility(View.GONE);
                        btnTolak.setVisibility(View.GONE);
                    }

                    if (item.getImagePath() != null) {
                        Glide.with(AdminItemDetailLostActivity.this)
                                .load("file://" + item.getImagePath())
                                .into(ivItemImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminItemDetailLostActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

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
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal verifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
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
        bottomNav.setSelectedItemId(R.id.nav_hilang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_barang) {
                startActivity(new Intent(this, LaporPenemuanActivity.class));
                return true;
            } else if (id == R.id.nav_hilang) {
                startActivity(new Intent(this, LaporKehilanganActivity.class));
                return true;
            }
            return false;
        });
    }
}