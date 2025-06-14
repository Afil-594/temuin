package com.example.temuin.lost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.temuin.R;

import java.io.File;
import java.util.List;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.ViewHolder> {
    private Context context;
    private List<LostItem> lostItemList;

    public LostItemAdapter(Context context, List<LostItem> lostItemList) {
        this.context = context;
        this.lostItemList = lostItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostItem item = lostItemList.get(position);
        holder.tvNamaBarang.setText(item.getNamaBarang() != null ? item.getNamaBarang() : "");
        holder.tvName.setText(item.getName() != null ? item.getName() : "");
        holder.tvNoHp.setText(item.getNoHp() != null ? item.getNoHp() : "");
        String statusText = item.getProgress() != null ? item.getProgress().toLowerCase() : "unknown";
        holder.tvStatus.setText(statusText);

        // Mengatur background berdasarkan status
        if ("dikembalikan".equals(statusText)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_red);
        }

        // Memuat gambar dari path
        try {
            String imagePath = item.getImagePath();
            if (imagePath != null) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.ivItemImage.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            holder.ivItemImage.setImageResource(android.R.color.darker_gray); // Placeholder jika gagal
        }

        // Mengatur aksi untuk button "Lihat Detail"
        holder.btnDetail.setOnClickListener(v -> {
            if (context instanceof Activity) {
                Intent intent = new Intent(context, DetailItemLostActivity.class);
                intent.putExtra("ITEM_ID", item.getId() != null ? item.getId() : "");
                intent.putExtra("ITEM_NAME", item.getName() != null ? item.getName() : "");
                intent.putExtra("ITEM_NO_HP", item.getNoHp() != null ? item.getNoHp() : "");
                intent.putExtra("ITEM_NAMA_BARANG", item.getNamaBarang() != null ? item.getNamaBarang() : "");
                intent.putExtra("ITEM_LOCATION", item.getLocation() != null ? item.getLocation() : "");
                intent.putExtra("ITEM_DATE", item.getDate() != null ? item.getDate() : "");
                intent.putExtra("ITEM_IMAGE", item.getImagePath() != null ? item.getImagePath() : "");
                intent.putExtra("ITEM_STATUS", statusText);
                ((Activity) context).startActivity(intent);
            } else {
                // Log atau toast untuk debugging
                // Toast.makeText(context, "Context tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        // Alternatif: Menggunakan itemView click listener (opsional, bisa dihapus jika button sudah cukup)
        // holder.itemView.setOnClickListener(v -> holder.btnDetail.performClick());
    }

    @Override
    public int getItemCount() {
        return lostItemList != null ? lostItemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNoHp, tvNamaBarang, tvStatus;
        ImageView ivItemImage;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNoHp = itemView.findViewById(R.id.tv_no_hp);
            tvNamaBarang = itemView.findViewById(R.id.tv_nama_barang);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}