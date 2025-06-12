package com.example.temuin.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.temuin.R;
import com.example.temuin.lost.LostItem;
import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.lost.LostItemsListActivity;
import com.example.temuin.HomeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.io.File;

public class AdminItemDetailLostActivity extends AppCompatActivity {

    private TextView tvName, tvNoHp, tvNamaBarang, tvLocation, tvDate, tvTime, tvStatus;
    private ImageView ivItemImage;
    private Button btnVerifikasi, btnTolak;
    private DatabaseReference databaseReference;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_item_detail);

        // Inisialisasi view
        tvName = findViewById(R.id.tv_detail_name);
        tvNoHp = findViewById(R.id.tv_detail_no_hp);
        tvNamaBarang = findViewById(R.id.tv_detail_name);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDate = findViewById(R.id.tv_detail_date);
        tvTime = findViewById(R.id.tv_detail_time);
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
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LostItem item = snapshot.getValue(LostItem.class);
                if (item != null) {
                    tvName.setText(item.getName());
                    tvNoHp.setText(item.getNoHp());
                    tvNamaBarang.setText(item.getNamaBarang());
                    tvLocation.setText(item.getLocation());
                    tvDate.setText(item.getDate());
                    tvTime.setText(item.getTime());
                    tvStatus.setText(item.getStatus());

                    if (item.getImagePath() != null) {
                        File file = new File(item.getImagePath());
                        if (file.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            ivItemImage.setImageBitmap(bitmap);
                        } else {
                            ivItemImage.setImageResource(android.R.color.darker_gray);
                        }
                    }
                } else {
                    Toast.makeText(AdminItemDetailLostActivity.this, "Data barang tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminItemDetailLostActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Tombol Verifikasi
        btnVerifikasi.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Verifikasi Barang")
                    .setMessage("Apakah Anda yakin ingin memverifikasi laporan ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        databaseReference.child("status").setValue("diverifikasi")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Laporan berhasil diverifikasi", Toast.LENGTH_SHORT).show();
                                    finish(); // Kembali ke list
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal verifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Tombol Tolak
        btnTolak.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tolak Laporan")
                    .setMessage("Apakah Anda yakin ingin menolak laporan ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        databaseReference.child("status").setValue("ditolak")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Laporan ditolak", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal menolak laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Bottom Nav (optional, bisa diubah sesuai kebutuhan admin)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_barang) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_laporan) {
                startActivity(new Intent(this, LostItemsListActivity.class));
                return true;
            }
            return false;
        });
    }
}
