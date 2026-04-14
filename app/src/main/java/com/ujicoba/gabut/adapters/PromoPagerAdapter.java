package com.ujicoba.gabut.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.MainActivity;

import java.util.List;

public class PromoPagerAdapter extends RecyclerView.Adapter<PromoPagerAdapter.ViewHolder> {

    private final List<MainActivity.PromoItem> promoList;
    private final Context context;

    public PromoPagerAdapter(Context context, List<MainActivity.PromoItem> promos) {
        this.context = context;
        this.promoList = promos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.PromoItem item = promoList.get(position);

        Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder) // bisa gambar loading atau abu2
                .error(R.drawable.ic_king)       // kalau gagal
                .into(holder.imageView);


        holder.imageView.setOnClickListener(v -> {
            // ✅ Cek apakah link tidak kosong atau null
            if (item.link != null && !item.link.trim().isEmpty()) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.link));
                    context.startActivity(browserIntent);
                } catch (Exception ignored) {
                    // Tidak perlu apa-apa, cukup diabaikan tanpa crash
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return promoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
