package com.ujicoba.gabut.adapters;


import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemGameBinding;
import com.ujicoba.gabut.models.GamesModel;
import com.ujicoba.gabut.utils.Methods;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GamesViewHolder> {

    private final List<GamesModel> gamesModelList;
    private OnGameItemClickListener onGameItemClickListener;
    private int lastPos = -1;

    private final int shimmerItems = 2;

    public void setOnGameItemClickListener(OnGameItemClickListener listener) {
        this.onGameItemClickListener = listener;
    }

    public interface OnGameItemClickListener {
        void onGameItemClick(String url, int p);
    }

    public GamesAdapter(List<GamesModel> gamesModelList) {
        this.gamesModelList = gamesModelList;
    }

    @NonNull
    @Override
    public GamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGameBinding binding = ItemGameBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GamesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GamesViewHolder holder, int position) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                holder.binding.gameLyt.setVisibility(View.VISIBLE);
                holder.binding.shimmerLayout.getRoot().setVisibility(View.GONE);
            }
        }, Methods.shimmerTime);

        Glide.with(holder.itemView.getContext())
                .load(gamesModelList.get(position).getGameLogo())
                .override(128, 128)
                .placeholder(R.drawable.logo)
                .into(holder.binding.gameLogo);
        holder.binding.gameName.setText(gamesModelList.get(position).getGameName());
        holder.binding.gameCoin.setText(String.format("%s %s", gamesModelList.get(position).getGameCoin(), holder.itemView.getContext().getResources().getString(R.string.text_coins)));
        holder.binding.gamePlayTime.setText(String.format("%s Min", gamesModelList.get(position).getGamePlayTime()));
        holder.binding.gameLayout.setOnClickListener(view -> {
            if (onGameItemClickListener != null) {
                onGameItemClickListener.onGameItemClick(gamesModelList.get(position).getGameLink(), position);
            }
        });
        setAnim(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return gamesModelList.size();
    }

    public class GamesViewHolder extends RecyclerView.ViewHolder{
        public final ItemGameBinding binding;
        public GamesViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private void setAnim(View view, int pos){
        if (pos > lastPos){
            Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_bottom);
            view.startAnimation(animation);
            lastPos = pos;
        }
    }
}
