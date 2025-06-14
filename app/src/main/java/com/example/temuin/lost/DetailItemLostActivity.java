package com.example.temuin.lost;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.temuin.HomeActivity;
import com.example.temuin.R;
import com.example.temuin.found.FoundItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailItemLostActivity extends AppCompatActivity {
    private TextView tvName, tvNoHp, tvNamaBarang, tvLocation, tvDate, tvStatus;
    private ImageView ivItemImage;
    private Button btnClaim;
    private DatabaseReference databaseReference;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item_lost);

        tvName = findViewById(R.id.tv_detail_name);
        tvNoHp = findViewById(R.id.tv_detail_no_hp);
        tvNamaBarang = findViewById(R.id.tv_detail_nama_barang);
        tvLocation = findViewById(R.id.tv_detail_location);
        tvDate = findViewById(R.id.tv_detail_date);
        tvStatus = findViewById(R.id.tv_detail_status);
        ivItemImage = findViewById(R.id.iv_detail_image);
        btnClaim = findViewById(R.id.btn_claim);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Mengambil itemId dari Intent (sesuaikan dengan key dari LostItemAdapter)
        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "ID barang tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items").child(itemId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LostItem item = snapshot.getValue(LostItem.class);
                if (item != null) {
                    tvName.setText(item.getName() != null ? item.getName() : "Tidak ada nama");
                    tvNoHp.setText(item.getNoHp() != null ? item.getNoHp() : "Tidak ada nomor HP");
                    tvNamaBarang.setText(item.getNamaBarang() != null ? item.getNamaBarang() : "Tidak ada nama barang");
                    tvLocation.setText(item.getLocation() != null ? item.getLocation() : "Tidak ada lokasi");
                    tvDate.setText(item.getDate() != null ? item.getDate() : "Tidak ada tanggal");
                    tvStatus.setText(item.getProgress() != null ? item.getProgress().toLowerCase() : "unknown");
                    if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                        Glide.with(DetailItemLostActivity.this).load("file://" + item.getImagePath()).into(ivItemImage);
                    } else {
                        ivItemImage.setImageResource(android.R.color.darker_gray);
                    }
                    if ("ditemukan".equals(item.getProgress())) {
                        btnClaim.setEnabled(false);
                        btnClaim.setText("Sudah Ditemukan");
                    }
                } else {
                    Toast.makeText(DetailItemLostActivity.this, "Data barang tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailItemLostActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            cekRoleDanJalankan(user, role -> {
                if (!"admin".equals(role)) {
                    btnClaim.setVisibility(View.GONE); // Sembunyikan kalau bukan admin
                } else {
                    btnClaim.setVisibility(View.VISIBLE); // Admin bisa lihat tombol
                }
            });
        }

        btnClaim.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Ditemukan")
                    .setMessage("Apakah Barang Sudah Ditemukan?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        databaseReference.child("progres").setValue("dikembalikan")
                                .addOnSuccessListener(aVoid -> {
                                    btnClaim.setEnabled(false);
                                    btnClaim.setText("Sudah Ditemukan");
                                    tvStatus.setText("dikembalikan");
                                    Toast.makeText(DetailItemLostActivity.this, "Barang berhasil ditemukan", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(DetailItemLostActivity.this, "Gagal mengklaim barang: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_hilang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(DetailItemLostActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(DetailItemLostActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                Intent intent = new Intent(DetailItemLostActivity.this, LostItemsListActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void cekRoleDanJalankan(FirebaseUser firebaseUser, RoleCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    callback.onRoleReceived(role);
                } else {
                    callback.onRoleReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onRoleReceived(null);
            }
        });
    }

    // Callback interface
    public interface RoleCallback {
        void onRoleReceived(String role);
    }


}