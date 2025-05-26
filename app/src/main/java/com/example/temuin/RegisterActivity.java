package com.example.temuin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNama, etJurusan, etEmail, etPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etNama = findViewById(R.id.etNama);
        etJurusan = findViewById(R.id.etJurusan);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = etNama.getText().toString().trim();
                String jurusan = etJurusan.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validasi input
                if (nama.isEmpty() || jurusan.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Validasi gagal: Ada field kosong");
                    return;
                }

                // Validasi format email
                if (!email.contains("@") || !email.contains(".")) {
                    Toast.makeText(RegisterActivity.this, "Email tidak valid!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Validasi gagal: Email tidak valid");
                    return;
                }

                // Validasi panjang password
                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Validasi gagal: Password terlalu pendek");
                    return;
                }

                // Sign up dengan Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Registrasi berhasil, UID: " + mAuth.getCurrentUser().getUid());
                                String userId = mAuth.getCurrentUser().getUid();

                                // Buat objek user
                                User user = new User(nama, jurusan, email);

                                // Simpan ke Firestore (tetep jalanin, tapi nggak blok transisi)
                                db.collection("users").document(userId).set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Data user berhasil disimpan ke Firestore");
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Gagal menyimpan data ke Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e(TAG, "Gagal menyimpan ke Firestore: ", e);
                                        });

                                // Pindah ke LoginActivity
                                Toast.makeText(RegisterActivity.this, "Registrasi sukses! Silakan login.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Registrasi gagal: ", task.getException());
                            }
                        });
            }
        });
    }

    public static class User {
        public String nama;
        public String jurusan;
        public String email;

        public User() {
            // Default constructor untuk Firestore
        }

        public User(String nama, String jurusan, String email) {
            this.nama = nama;
            this.jurusan = jurusan;
            this.email = email;
        }
    }
}