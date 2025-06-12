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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNama, etJurusan, etEmail, etPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
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
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        etNama = findViewById(R.id.etNama);
        etJurusan = findViewById(R.id.etJurusan);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String jurusan = etJurusan.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nama.isEmpty() || jurusan.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Validasi gagal: Ada field kosong");
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(RegisterActivity.this, "Email tidak valid!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Validasi gagal: Email tidak valid");
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Validasi gagal: Password terlalu pendek");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser == null) return;

                            String userId = firebaseUser.getUid();
                            User user = new User(nama, jurusan, email, "user"); // default role: user

                            dbRef.child(userId).setValue(user)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Data user berhasil disimpan ke Realtime DB"))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "Error simpan user: ", e);
                                    });

                            Toast.makeText(RegisterActivity.this, "Registrasi sukses! Silakan login.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Registrasi gagal: ", task.getException());
                        }
                    });
        });
    }

    public static class User {
        public String nama;
        public String jurusan;
        public String email;
        public String role;

        public User() {}

        public User(String nama, String jurusan, String email, String role) {
            this.nama = nama;
            this.jurusan = jurusan;
            this.email = email;
            this.role = role;
        }
    }
}
