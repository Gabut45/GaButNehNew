package com.ujicoba.gabut.models;

public class LeaderboardModel {
    private String userImg;
    private String userName;
    private String userCoins;

    public LeaderboardModel(String userImg, String userName, String userCoins) {
        this.userImg = userImg;
        this.userName = userName;
        this.userCoins = userCoins;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCoins() {
        return userCoins;
    }

    public void setUserCoins(String userCoins) {
        this.userCoins = userCoins;
    }
}
