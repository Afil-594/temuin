package com.example.temuin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    private ImageView btnProfile;
    private LinearLayout profileOverlay;
    private Button btnLogout;
    private TextView tvNama, tvJurusan, tvEmail, tvWelcome;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();
        btnProfile = findViewById(R.id.btnProfile);
        profileOverlay = findViewById(R.id.profileOverlay);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.textWelcome);
        tvNama = findViewById(R.id.tvNama);
        tvJurusan = findViewById(R.id.tvJurusan);
        tvEmail = findViewById(R.id.tvEmail);

        // Profile overlay
        btnProfile.setOnClickListener(v -> {
            if (profileOverlay.getVisibility() == View.GONE) {
                profileOverlay.setVisibility(View.VISIBLE);
                profileOverlay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            } else {
                Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        profileOverlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                profileOverlay.startAnimation(slideOut);
            }
        });

        // Isi user
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nama = documentSnapshot.getString("nama");
                        String jurusan = documentSnapshot.getString("jurusan");
                        String email = documentSnapshot.getString("email");

                        // Update UI
                        tvWelcome.setText("Selamat Datang, " + nama);
                        tvNama.setText(nama);
                        tvJurusan.setText(jurusan);
                        tvEmail.setText(email);
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}