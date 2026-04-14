package com.ujicoba.gabut.models;

public class GamesModel {
    private int id;
    private int cat_id;
    private String gameLogo;
    private String gameName;
    private String gameCoin;
    private String gamePlayTime;
    private String gameLink;
    private String gameCost;

    public GamesModel(int id, int cat_id, String gameLogo, String gameName, String gameCoin, String gamePlayTime, String gameLink, String gameCost) {
        this.id = id;
        this.cat_id = cat_id;
        this.gameLogo = gameLogo;
        this.gameName = gameName;
        this.gameCoin = gameCoin;
        this.gamePlayTime = gamePlayTime;
        this.gameLink = gameLink;
        this.gameCost = gameCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getGameLogo() {
        return gameLogo;
    }

    public void setGameLogo(String gameLogo) {
        this.gameLogo = gameLogo;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameCoin() {
        return gameCoin;
    }

    public void setGameCoin(String gameCoin) {
        this.gameCoin = gameCoin;
    }

    public String getGamePlayTime() {
        return gamePlayTime;
    }

    public void setGamePlayTime(String gamePlayTime) {
        this.gamePlayTime = gamePlayTime;
    }

    public String getGameLink() {
        return gameLink;
    }

    public void setGameLink(String gameLink) {
        this.gameLink = gameLink;
    }

    public String getGameCost() {
        return gameCost;
    }

    public void setGameCost(String gameCost) {
        this.gameCost = gameCost;
    }
}
