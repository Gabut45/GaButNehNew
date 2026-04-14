package com.ujicoba.gabut.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemLeaderboardBinding;
import com.ujicoba.gabut.models.LeaderboardModel;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderViewHolder> {
    private List<LeaderboardModel> leaderboardModelList;
    public boolean showShimmer = true;
    private final int shimmerItems = 5;
    private int lastPos = -1;
    private Context context;

    // Constructor yang menerima context sebagai parameter
    public LeaderboardAdapter(List<LeaderboardModel> leaderboardModelList, Context context) {
        this.leaderboardModelList = leaderboardModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public LeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LeaderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderViewHolder holder, int position) {
        if (showShimmer) {
            holder.binding.leaderboardLyt.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.startShimmer();
        } else {
            holder.binding.leaderboardLyt.setVisibility(View.VISIBLE);
            holder.binding.shimmerLayout.shimmerContainer.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.stopShimmer();
            holder.binding.shimmerLayout.shimmerContainer.setShimmer(null);

            Glide.with(holder.itemView.getContext())
                    .load(leaderboardModelList.get(position).getUserImg())
                    .override(128, 128)
                    .placeholder(R.drawable.logo)
                    .into(holder.binding.userImg);

            holder.binding.userName.setText(leaderboardModelList.get(position).getUserName());
            holder.binding.userCoins.setText(leaderboardModelList.get(position).getUserCoins());
            holder.binding.userPos.setText("#" + (position + 1));

            // Set click listener untuk item leaderboard
            holder.itemView.setOnClickListener(v -> {
            //    showAnimatedDialog();
            });

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (position == getItemCount() - 1) {
                layoutParams.setMargins(0, 0, 0, 200);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            holder.itemView.setLayoutParams(layoutParams);
            setAnim(holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        return showShimmer ? shimmerItems : leaderboardModelList.size();
    }

    public static class LeaderViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderboardBinding binding;

        public LeaderViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void setAnim(View view, int pos) {
        if (pos > lastPos) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_bottom);
            view.startAnimation(animation);
            lastPos = pos;
        }
    }


}
