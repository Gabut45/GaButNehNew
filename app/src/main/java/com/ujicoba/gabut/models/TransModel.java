package com.ujicoba.gabut.models;

public class TransModel {
    private String title;
    private String coins;
    private String date;

    public TransModel(String title, String coins, String date) {
        this.title = title;
        this.coins = coins;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoins() {
        return coins;
    }

    public void setCoins(String coins) {
        this.coins = coins;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
