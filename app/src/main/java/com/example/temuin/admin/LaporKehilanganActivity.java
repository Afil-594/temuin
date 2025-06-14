package com.example.temuin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.temuin.R;
import com.example.temuin.found.FoundItemsListActivity;
import com.example.temuin.found.ReportFoundItemActivity;
import com.example.temuin.lost.LostItem;
import com.example.temuin.lost.LostItemsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LaporKehilanganActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminLostItemAdapter adapter;
    private List<LostItem> itemList;
    private FloatingActionButton fabAdd;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lapor_kehilangan);

        recyclerView = findViewById(R.id.rv_lost_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        adapter = new AdminLostItemAdapter(this, itemList);
        btnBack = findViewById(R.id.btn_back);
        recyclerView.setAdapter(adapter);
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

        loadPendingReports();

        btnBack.setOnClickListener(v -> finish());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_hilang);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_barang) {
                startActivity(new Intent(LaporKehilanganActivity.this, LaporPenemuanActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(LaporKehilanganActivity.this, AdminDashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_hilang) {
                return true;
            }
            return false;
        });
    }

    private void loadPendingReports() {
        FirebaseDatabase.getInstance()
                .getReference("lost_items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            LostItem item = data.getValue(LostItem.class);
                            if (item != null) {
                                if (item.isAdminArchived()) {
                                    continue;
                                }
                                itemList.add(item);
                            }
                        }
                        java.util.Collections.sort(itemList, (item1, item2) -> {
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
                        Toast.makeText(LaporKehilanganActivity.this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LaporKehilangan", "onCancelled: ", error.toException());
                    }
                });
    }
}
