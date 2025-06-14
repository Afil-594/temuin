package com.example.temuin.lost;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.temuin.HomeActivity;
import com.example.temuin.R;
import com.example.temuin.found.FoundItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LostItemsListActivity extends AppCompatActivity {
    private RecyclerView rvLostItems;
    private LostItemAdapter adapter;
    private List<LostItem> lostItemList;
    private Query databaseReference;
    private EditText etSearch;
    private ImageButton btnBack;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_items_list);

        rvLostItems = findViewById(R.id.rv_lost_items);
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        fabAdd = findViewById(R.id.fab_add);

        if (fabAdd == null) {
            Toast.makeText(this, "FAB tidak ditemukan!", Toast.LENGTH_SHORT).show();
            Log.e("LostItemsListActivity", "FloatingActionButton fab_add tidak ditemukan di layout");
            return;
        }

        fabAdd.setOnClickListener(v -> {
            Log.d("LostItemsListActivity", "FAB diklik, memulai intent...");
            Intent intent = new Intent(this, ReportLostItemActivity.class);
            try {
                startActivity(intent);
                Log.d("LostItemsListActivity", "Intent ke ReportLostItemActivity berhasil dimulai");
            } catch (Exception e) {
                Log.e("LostItemsListActivity", "Gagal memulai intent: " + e.getMessage());
                Toast.makeText(this, "Gagal membuka form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rvLostItems.setLayoutManager(new LinearLayoutManager(this));
        lostItemList = new ArrayList<>();
        adapter = new LostItemAdapter(this, lostItemList);
        rvLostItems.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("lost_items")
                .orderByChild("status")
                .equalTo("diverifikasi");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lostItemList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    LostItem item = data.getValue(LostItem.class);
                    if (item != null) {
                        lostItemList.add(item);
                    }
                }
                java.util.Collections.sort(lostItemList, (item1, item2) -> {
                    String progress1 = item1.getProgress();
                    String progress2 = item2.getProgress();

                    int weight1 = progress1.equals("hilang") ? 0 : (progress1.equals("ditemukan") ? 1 : 2);
                    int weight2 = progress2.equals("hilang") ? 0 : (progress2.equals("ditemukan") ? 1 : 2);

                    return Integer.compare(weight1, weight2);
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LostItemsListActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}

            private void filterList(String text) {
                List<LostItem> filteredList = new ArrayList<>();
                for (LostItem item : lostItemList) {
                    if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                adapter = new LostItemAdapter(LostItemsListActivity.this, filteredList);
                rvLostItems.setAdapter(adapter);
            }
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(LostItemsListActivity.this, FoundItemsListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(LostItemsListActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_hilang);
    }
}