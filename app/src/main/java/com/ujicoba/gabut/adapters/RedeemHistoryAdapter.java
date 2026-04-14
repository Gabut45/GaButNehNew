package com.ujicoba.gabut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemRedeemHistoryBinding;
import com.ujicoba.gabut.models.RedeemHistoryModel;
import com.ujicoba.gabut.utils.Methods;

import java.util.List;

public class RedeemHistoryAdapter extends RecyclerView.Adapter<RedeemHistoryAdapter.RedeemViewHolder> {
    private List<RedeemHistoryModel> redeemHistoryModelList;
    public boolean showShimmer = true;
    private int shimmerItems = 10;
    private int lastPos = -1;

    public RedeemHistoryAdapter(List<RedeemHistoryModel> redeemHistoryModelList) {
        this.redeemHistoryModelList = redeemHistoryModelList;
    }

    @NonNull
    @Override
    public RedeemHistoryAdapter.RedeemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRedeemHistoryBinding binding = ItemRedeemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RedeemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemHistoryAdapter.RedeemViewHolder holder, int position) {
        if (showShimmer){
            holder.binding.redeemHistoryLyt.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.startShimmer();
        } else {
            holder.binding.redeemHistoryLyt.setVisibility(View.VISIBLE);
            holder.binding.shimmerLayout.shimmerContainer.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.stopShimmer();
            holder.binding.shimmerLayout.shimmerContainer.setShimmer(null);
            holder.binding.methodName.setText(redeemHistoryModelList.get(position).getPayment_method());
            holder.binding.date.setText(redeemHistoryModelList.get(position).getRedeem_date());
            holder.binding.coins.setText(redeemHistoryModelList.get(position).getRedeem_coins());
            String status = redeemHistoryModelList.get(position).getPayment_status();
            holder.binding.status.setText(Methods.textCapitalize(status));
            if (status.equalsIgnoreCase("pending"))
                holder.binding.status.setBackgroundResource(R.drawable.bg_pending);
            else if (status.equalsIgnoreCase("approved"))
                holder.binding.status.setBackgroundResource(R.drawable.bg_success);
            else if (status.equalsIgnoreCase("rejected"))
                holder.binding.status.setBackgroundResource(R.drawable.bg_reject);
            setAnim(holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        return showShimmer ? shimmerItems : redeemHistoryModelList.size();
    }

    public class RedeemViewHolder extends RecyclerView.ViewHolder {
        ItemRedeemHistoryBinding binding;
        public RedeemViewHolder(ItemRedeemHistoryBinding binding) {
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
