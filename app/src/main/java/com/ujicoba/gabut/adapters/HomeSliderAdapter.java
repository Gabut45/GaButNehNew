package com.ujicoba.gabut.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemHomeSliderBinding;
import com.ujicoba.gabut.models.HomeSliderModel;

import java.util.List;

public class HomeSliderAdapter extends RecyclerView.Adapter<HomeSliderAdapter.HomeSliderViewHolder> {

    private List<HomeSliderModel> homeSliderModelList;
    private ViewPager2 viewPager2;

    public HomeSliderAdapter(List<HomeSliderModel> homeSliderModelList, ViewPager2 viewPager2) {
        this.homeSliderModelList = homeSliderModelList;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public HomeSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeSliderBinding binding = ItemHomeSliderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeSliderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeSliderViewHolder holder, int position) {
        holder.binding.shimmerContainer.startShimmer();
        holder.binding.shimmerContainer.setVisibility(View.VISIBLE);
        holder.binding.sliderImg.setVisibility(View.GONE);
        Glide.with(holder.itemView.getContext())
                .load(homeSliderModelList.get(position).getSliderImgUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(600, 400)
                .placeholder(R.drawable.slider_1)
                .error(R.drawable.slider_1)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.binding.shimmerContainer.startShimmer();
                        holder.binding.shimmerContainer.setVisibility(View.GONE);
                        holder.binding.sliderImg.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                holder.binding.shimmerContainer.startShimmer();
                                holder.binding.shimmerContainer.setVisibility(View.GONE);
                                holder.binding.sliderImg.setVisibility(View.VISIBLE);
                            }
                        }, 1000);
                        return false;
                    }
                })
                .into(holder.binding.sliderImg);
        if (position == homeSliderModelList.size() - 1){
            viewPager2.post(runnable);
        }

        //   <!-- Bagian ini fungsinya agar ketika tidak ada link maka tidak terjadi apa2 -->

        holder.binding.sliderImg.setOnClickListener(view -> {
            String link = homeSliderModelList.get(position).getSliderLink();
            if (link != null && !link.trim().isEmpty()) {
                holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return homeSliderModelList.size();
    }

    public class HomeSliderViewHolder extends RecyclerView.ViewHolder{
        public final ItemHomeSliderBinding binding;
        public HomeSliderViewHolder(ItemHomeSliderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            homeSliderModelList.addAll(homeSliderModelList);
            notifyDataSetChanged();
        }
    };
}
