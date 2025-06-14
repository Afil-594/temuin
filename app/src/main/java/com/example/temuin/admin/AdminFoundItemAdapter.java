package com.example.temuin.admin;

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
import com.example.temuin.found.FoundItem;
import com.example.temuin.found.FoundItemAdapter;

import java.io.File;
import java.util.List;

public class AdminFoundItemAdapter extends RecyclerView.Adapter<AdminFoundItemAdapter.ViewHolder> {
    private Context context;
    private List<FoundItem> foundItemList;

    public AdminFoundItemAdapter(Context context, List<FoundItem> foundItemList) {
        this.context = context;
        this.foundItemList = foundItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_found, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoundItem item = foundItemList.get(position);
        holder.tvName.setText(item.getName() != null ? item.getName() : "");
        holder.tvLocation.setText(item.getLocation() != null ? item.getLocation() : "");
        holder.tvDate.setText(item.getDate() != null ? item.getDate() : "");
        String statusText = item.getStatus() != null ? item.getStatus().toLowerCase() : "unknown";
        holder.tvStatus.setText(statusText);

        // Mengatur background berdasarkan status
        if ("diverifikasi".equals(statusText)) {
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
            holder.ivItemImage.setImageResource(android.R.color.darker_gray);
        }

        // Mengatur aksi untuk button "Lihat Detail"
        holder.btnDetail.setOnClickListener(v -> {
            if (context instanceof Activity) {
                Intent intent = new Intent(context, AdminItemDetailActivity.class);
                intent.putExtra("ITEM_ID", item.getId() != null ? item.getId() : "");
                intent.putExtra("ITEM_NAME", item.getName() != null ? item.getName() : "");
                intent.putExtra("ITEM_LOCATION", item.getLocation() != null ? item.getLocation() : "");
                intent.putExtra("ITEM_DATE", item.getDate() != null ? item.getDate() : "");
                intent.putExtra("ITEM_TIME", item.getTime() != null ? item.getTime() : "");
                intent.putExtra("ITEM_IMAGE", item.getImagePath() != null ? item.getImagePath() : "");
                intent.putExtra("ITEM_STATUS", statusText);
                ((Activity) context).startActivity(intent);
            }
        });

        // Alternatif: Menggunakan itemView click listener (opsional, bisa dihapus jika button sudah cukup)
        // holder.itemView.setOnClickListener(v -> holder.btnDetail.performClick());
    }

    @Override
    public int getItemCount() {
        return foundItemList != null ? foundItemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvDate, tvStatus;
        ImageView ivItemImage;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}