package com.ujicoba.gabut.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ujicoba.gabut.R;
import com.ujicoba.gabut.activities.GameListActivity;
import com.ujicoba.gabut.models.GameCategory;

import java.util.List;

public class GameCategoryAdapter extends RecyclerView.Adapter<GameCategoryAdapter.CategoryViewHolder> {

    private List<GameCategory> categories;
    private boolean isHideViewAllBtn = false;
    private OnGameItemClickListener onGameItemClickListener;

    public void setOnGameItemClickListener(OnGameItemClickListener listener) {
        this.onGameItemClickListener = listener;
    }

    public interface OnGameItemClickListener {
        void onGameItemClick(String url, int catPos, int gamePos);
    }

    public GameCategoryAdapter(List<GameCategory> categories, boolean isHideViewAllBtn) {
        this.categories = categories;
        this.isHideViewAllBtn = isHideViewAllBtn;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {

            GameCategory category = categories.get(position);
            holder.categoryName.setText(category.getCatName());

            if (isHideViewAllBtn){
                holder.viewAllBtn.setVisibility(View.GONE);
            } else {
                holder.viewAllBtn.setOnClickListener(view -> {
                    Intent intent = new Intent(holder.itemView.getContext(), GameListActivity.class);
                    intent.putExtra("cat_id", category.getCatId());
                    holder.itemView.getContext().startActivity(intent);
                });
            }

            GamesAdapter gameAdapter = new GamesAdapter(category.getGamesModelLIst());
            holder.gamesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.gamesRecyclerView.setAdapter(gameAdapter);

            gameAdapter.setOnGameItemClickListener(new GamesAdapter.OnGameItemClickListener() {
                @Override
                public void onGameItemClick(String url, int gamePos) {
                    onGameItemClickListener.onGameItemClick(url, position, gamePos);
                }
            });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView viewAllBtn;
        RecyclerView gamesRecyclerView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            viewAllBtn = itemView.findViewById(R.id.viewAllBtn);
            gamesRecyclerView = itemView.findViewById(R.id.games_recyclerview);
        }
    }
}
