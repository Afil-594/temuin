package com.example.temuin.admin;

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
import com.example.temuin.found.FoundItem;
import com.example.temuin.found.ReportFoundItemActivity;
import com.example.temuin.lost.DetailItemLostActivity;
import com.example.temuin.lost.LostItemsListActivity;
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

public class LaporPenemuanActivity extends AppCompatActivity {
    private RecyclerView rvFoundItems;
    private AdminFoundItemAdapter adapter;
    private List<FoundItem> foundItemList;
    private EditText etSearch;
    private ImageButton btnBack;
    private FloatingActionButton fabAdd;
    private Query databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_items_list);

        rvFoundItems = findViewById(R.id.rv_found_items);
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        fabAdd = findViewById(R.id.fab_add);

        if (fabAdd == null) {
            Toast.makeText(this, "FAB tidak ditemukan!", Toast.LENGTH_SHORT).show();
            Log.e("FoundItemsListActivity", "FloatingActionButton fab_add tidak ditemukan di layout");
            return;
        }

        fabAdd.setOnClickListener(v -> {
            Log.d("FoundItemsListActivity", "FAB diklik, memulai intent...");
            Intent intent = new Intent(this, ReportFoundItemActivity.class);
            try {
                startActivity(intent);
                Log.d("FoundItemsListActivity", "Intent ke ReportFoundItemActivity berhasil dimulai");
            } catch (Exception e) {
                Log.e("FoundItemsListActivity", "Gagal memulai intent: " + e.getMessage());
                Toast.makeText(this, "Gagal membuka form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        rvFoundItems.setLayoutManager(new LinearLayoutManager(this));
        foundItemList = new ArrayList<>();
        adapter = new AdminFoundItemAdapter(this, foundItemList);
        rvFoundItems.setAdapter(adapter);

        Query databaseReference = FirebaseDatabase.getInstance().getReference("found_items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foundItemList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FoundItem item = data.getValue(FoundItem.class);
                    if (item != null) {
                        if (item.isAdminArchived()) {
                            continue;
                        }
                        foundItemList.add(item);
                    }
                }
                java.util.Collections.sort(foundItemList, (item1, item2) -> {
                    String status1 = item1.getStatus();
                    String status2 = item2.getStatus();

                    int weight1 = status1.equals("belum diverifikasi") ? 0 :
                            status1.equals("diverifikasi") ? 1 :
                                    status1.equals("ditolak") ? 2 : 3;

                    int weight2 = status2.equals("belum diverifikasi") ? 0 :
                            status2.equals("diverifikasi") ? 1 :
                                    status2.equals("ditolak") ? 2 : 3;

                    return Integer.compare(weight1, weight2);
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LaporPenemuanActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                List<FoundItem> filteredList = new ArrayList<>();
                for (FoundItem item : foundItemList) {
                    if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                adapter = new AdminFoundItemAdapter(LaporPenemuanActivity.this, filteredList);
                rvFoundItems.setAdapter(adapter);
            }
        });

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_barang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(LaporPenemuanActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                Intent intent = new Intent(LaporPenemuanActivity.this, LaporKehilanganActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}