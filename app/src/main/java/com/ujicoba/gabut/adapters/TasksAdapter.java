package com.ujicoba.gabut.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ujicoba.gabut.R;
import com.ujicoba.gabut.databinding.ItemTaskBinding;
import com.ujicoba.gabut.models.TasksModel;

import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {

    private List<TasksModel> tasksModelList;
    Context context;
    private OnTaskItemClickListener onTaskItemClickListener;
    public boolean showShimmer = true;
    private int shimmerItems = 10;
    private int lastPos = -1;

    public void setOnTaskItemClickListener(OnTaskItemClickListener listener) {
        this.onTaskItemClickListener = listener;
    }

    public interface OnTaskItemClickListener {
        void onTaskItemClick(String url, int p);
    }

    public TasksAdapter(List<TasksModel> tasksModelList, Context context) {
        this.tasksModelList = tasksModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public TasksAdapter.TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TasksViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksAdapter.TasksViewHolder holder, int position) {
        if (showShimmer){
            holder.binding.taskLayout.setVisibility(View.GONE);
            holder.binding.shimmerLayout.shimmerContainer.startShimmer();
        } else {
            holder.binding.taskLayout.setVisibility(View.VISIBLE);
            holder.binding.shimmerLayout.shimmerContainer.stopShimmer();
            holder.binding.shimmerLayout.shimmerContainer.setShimmer(null);
            holder.binding.shimmerLayout.shimmerContainer.setVisibility(View.GONE);
            Glide.with(context)
                    .load(tasksModelList.get(position).getTask_logo())
                    .override(128, 128)
                    .placeholder(R.drawable.logo)
                    .into(holder.binding.taskIcon);
            holder.binding.taskTime.setText(String.format("%sm", tasksModelList.get(position).getTask_time()));
            holder.binding.taskTitle.setText(tasksModelList.get(position).getTask_name());
            holder.binding.taskDesc.setText(tasksModelList.get(position).getTask_desc());
            holder.binding.taskCoin.setText(tasksModelList.get(position).getTask_coin());
            holder.binding.taskLayout.setOnClickListener(view -> {
                onTaskItemClickListener.onTaskItemClick(tasksModelList.get(position).getTask_link(), position);
            });
            setAnim(holder.itemView, position);
        }

    }

    @Override
    public int getItemCount() {
        return showShimmer ? shimmerItems : tasksModelList.size();
    }

    public class TasksViewHolder extends RecyclerView.ViewHolder {
        ItemTaskBinding binding;
        public TasksViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private void setAnim(View view, int pos){
        if (pos > lastPos){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
            view.startAnimation(animation);
            lastPos = pos;
        }
    }
}
