package com.example.temuin.lost;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.temuin.HomeActivity;
import com.example.temuin.R;
import com.example.temuin.found.FoundItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportLostItemActivity extends AppCompatActivity {
    private EditText etName, etNoHp, etNamaBarang, etLocation, etDate;
    private ImageView ivItemImage;
    private Button btnSubmit;
    private DatabaseReference databaseReference;
    private String imagePath;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost_item);
        Log.d("ReportLostItemActivity", "Activity dimuat");

        etName = findViewById(R.id.et_name);
        etNoHp = findViewById(R.id.et_no_hp);
        etNamaBarang = findViewById(R.id.et_nama_barang);
        etLocation = findViewById(R.id.et_location);
        etDate = findViewById(R.id.et_date);
        ivItemImage = findViewById(R.id.iv_item_image);
        btnSubmit = findViewById(R.id.btn_submit);

        if (etName == null || etNoHp == null || etNamaBarang == null || etLocation == null || etDate == null || ivItemImage == null || btnSubmit == null) {
            Log.e("ReportLostItemActivity", "Salah satu elemen UI tidak ditemukan");
            Toast.makeText(this, "Error: Elemen UI tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items");

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d("ReportLostItemActivity", "onActivityResult dipanggil, resultCode: " + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            Uri imageUri = data.getData();
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (bitmap != null) {
                                    imagePath = saveImageToInternalStorage(bitmap);
                                    ivItemImage.setImageBitmap(bitmap);
                                } else {
                                    Toast.makeText(ReportLostItemActivity.this, "Gagal memuat gambar: Bitmap null", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                Log.e("ReportLostItemActivity", "Gagal memuat gambar: " + e.getMessage());
                                Toast.makeText(ReportLostItemActivity.this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("ReportLostItemActivity", "Pemilihan gambar dibatalkan atau gagal");
                        }
                    }
                }
        );

        ivItemImage.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    openGallery();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    openGallery();
                }
            } else {
                openGallery();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String noHp = etNoHp.getText().toString().trim();
            String namaBarang = etNamaBarang.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (name.isEmpty() ||  noHp.isEmpty() ||  namaBarang.isEmpty() || location.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imagePath == null) {
                Toast.makeText(this, "Silakan unggah foto barang", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";
            String itemId = databaseReference.push().getKey();
            LostItem item = new LostItem(itemId, name, noHp, namaBarang, location, date, imagePath, "belum diverifikasi", "hilang", userId);

            databaseReference.child(itemId).setValue(item)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Barang berhasil dilaporkan", Toast.LENGTH_SHORT).show();
                        // Arahkan ke FoundItemsListActivity
                        Intent intent = new Intent(ReportLostItemActivity.this, LostItemsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal melaporkan barang", Toast.LENGTH_SHORT).show());
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_hilang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(ReportLostItemActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ReportLostItemActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                startActivity(new Intent(ReportLostItemActivity.this, LostItemsListActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        Toast.makeText(this, "Izin penyimpanan ditolak. Silakan aktifkan di pengaturan aplikasi.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Izin penyimpanan diperlukan untuk memilih gambar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Izin penyimpanan diperlukan untuk memilih gambar", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(directory, "IMG_" + timeStamp + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }
}