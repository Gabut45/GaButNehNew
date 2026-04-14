package com.ujicoba.gabut.models;

public class RedeemHistoryModel {
    private String redeem_coins;
    private String payment_method;
    private String payment_status;
    private String redeem_date;

    public RedeemHistoryModel(String redeem_coins, String payment_method, String payment_status, String redeem_date) {
        this.redeem_coins = redeem_coins;
        this.payment_method = payment_method;
        this.payment_status = payment_status;
        this.redeem_date = redeem_date;
    }

    public String getRedeem_coins() {
        return redeem_coins;
    }

    public void setRedeem_coins(String redeem_coins) {
        this.redeem_coins = redeem_coins;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getRedeem_date() {
        return redeem_date;
    }

    public void setRedeem_date(String redeem_date) {
        this.redeem_date = redeem_date;
    }
}
