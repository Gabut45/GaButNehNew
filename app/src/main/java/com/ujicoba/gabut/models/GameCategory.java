package com.ujicoba.gabut.models;

import java.util.List;

public class GameCategory {
    private int catId;
    private String catName;
    private List<GamesModel> gamesModelLIst;

    public GameCategory(int catId, String catName, List<GamesModel> gamesModelLIst) {
        this.catId = catId;
        this.catName = catName;
        this.gamesModelLIst = gamesModelLIst;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public List<GamesModel> getGamesModelLIst() {
        return gamesModelLIst;
    }

    public void setGamesModelLIst(List<GamesModel> gamesModelLIst) {
        this.gamesModelLIst = gamesModelLIst;
    }
}
