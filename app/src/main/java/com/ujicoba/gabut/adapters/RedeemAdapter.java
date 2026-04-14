package com.ujicoba.gabut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemRedeemBinding;
import com.ujicoba.gabut.models.RedeemModel;

import java.util.List;

public class RedeemAdapter extends RecyclerView.Adapter<RedeemAdapter.RedeemViewHolder> {
    private List<RedeemModel> redeemModelList;
    private OnRedeemItemClickListener onRedeemItemClickListener;
    public boolean showShimmer = true;
    private int shimmerItems = 8;
    private int lastPos = -1;

    public void setOnRedeemItemClickListener(OnRedeemItemClickListener listener) {
        this.onRedeemItemClickListener = listener;
    }

    public interface OnRedeemItemClickListener {
        void onRedeemItemClick(int p);
    }

    public RedeemAdapter(List<RedeemModel> redeemModelList) {
        this.redeemModelList = redeemModelList;
    }

    @NonNull
    @Override
    public RedeemAdapter.RedeemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRedeemBinding binding = ItemRedeemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RedeemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemAdapter.RedeemViewHolder holder, int position) {
        if (showShimmer){
            holder.binding.redeemLyt.setVisibility(View.GONE);
            holder.binding.shimmerContainer.startShimmer();
        } else {
            holder.binding.redeemLyt.setVisibility(View.VISIBLE);
            holder.binding.shimmerContainer.setVisibility(View.GONE);
            holder.binding.shimmerContainer.stopShimmer();
            holder.binding.shimmerContainer.setShimmer(null);
            Glide.with(holder.itemView.getContext())
                    .load(redeemModelList.get(position).getCoin_image())
                    .override(256, 256)
                    .placeholder(R.drawable.logo)
                    .into(holder.binding.redeemCoinImage);
            holder.binding.redeemPaymentMethod.setText(holder.itemView.getContext().getString(R.string.text_method)+"\n"+redeemModelList.get(position).getPayment_method());
            holder.binding.redeemCoinText.setText(redeemModelList.get(position).getRedeem_coins());
            holder.binding.redeemDesc.setText(redeemModelList.get(position).getRedeem_desc());
            holder.binding.withdrawBtn.setText(redeemModelList.get(position).getButton_text());
            holder.binding.withdrawBtn.setOnClickListener(view -> {
                onRedeemItemClickListener.onRedeemItemClick(position);
            });
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (getItemCount() > 1 && position >= getItemCount() - 2) {
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
        return showShimmer ? shimmerItems : redeemModelList.size();
    }

    public class RedeemViewHolder extends RecyclerView.ViewHolder {
        ItemRedeemBinding binding;
        public RedeemViewHolder(ItemRedeemBinding binding) {
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
