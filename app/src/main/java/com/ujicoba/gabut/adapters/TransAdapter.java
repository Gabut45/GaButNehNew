package com.ujicoba.gabut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemTransactionBinding;
import com.ujicoba.gabut.models.TransModel;

import java.util.List;

public class TransAdapter extends RecyclerView.Adapter<TransAdapter.TransViewHolder> {
    private List<TransModel> transModelList;
    public boolean showShimmer = true;
    private final int shimmerItems = 10;
    private int lastPos = -1;

    public TransAdapter(List<TransModel> transModelList) {
        this.transModelList = transModelList;
    }

    @NonNull
    @Override
    public TransAdapter.TransViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransAdapter.TransViewHolder holder, int position) {
        if (showShimmer){
            holder.binding.transLayout.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.startShimmer();
        } else {
            holder.binding.transLayout.setVisibility(View.VISIBLE);
            holder.binding.shimmerLayout.shimmerContainer.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.stopShimmer();
            holder.binding.shimmerLayout.shimmerContainer.setShimmer(null);
            holder.binding.transTitle.setText(transModelList.get(position).getTitle());
            holder.binding.transDate.setText(transModelList.get(position).getDate());
            holder.binding.transCoin.setText(transModelList.get(position).getCoins());
            setAnim(holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        return showShimmer ? shimmerItems : transModelList.size();
    }

    public class TransViewHolder extends RecyclerView.ViewHolder {
        ItemTransactionBinding binding;
        public TransViewHolder(ItemTransactionBinding binding) {
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
