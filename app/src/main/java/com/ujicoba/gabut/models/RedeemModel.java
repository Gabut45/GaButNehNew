package com.ujicoba.gabut.models;

public class RedeemModel {
    private int id;
    private String coin_image;
    private String redeem_coins;
    private String redeem_desc;
    private String payment_method;
    private String button_text;

    public RedeemModel(int id, String coin_image, String redeem_coins, String redeem_desc, String payment_method, String button_text) {
        this.id = id;
        this.coin_image = coin_image;
        this.redeem_coins = redeem_coins;
        this.redeem_desc = redeem_desc;
        this.payment_method = payment_method;
        this.button_text = button_text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoin_image() {
        return coin_image;
    }

    public void setCoin_image(String coin_image) {
        this.coin_image = coin_image;
    }

    public String getRedeem_coins() {
        return redeem_coins;
    }

    public void setRedeem_coins(String redeem_coins) {
        this.redeem_coins = redeem_coins;
    }

    public String getRedeem_desc() {
        return redeem_desc;
    }

    public void setRedeem_desc(String redeem_desc) {
        this.redeem_desc = redeem_desc;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getButton_text() {
        return button_text;
    }

    public void setButton_text(String button_text) {
        this.button_text = button_text;
    }
}
