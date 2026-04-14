package com.ujicoba.gabut.models;

public class HomeSliderModel {
    private String sliderImgUrl;
    private String sliderLink;

    public HomeSliderModel(String sliderImgUrl, String sliderLink) {
        this.sliderImgUrl = sliderImgUrl;
        this.sliderLink = sliderLink;
    }

    public String getSliderImgUrl() {
        return sliderImgUrl;
    }

    public void setSliderImgUrl(String sliderImgUrl) {
        this.sliderImgUrl = sliderImgUrl;
    }

    public String getSliderLink() {
        return sliderLink;
    }

    public void setSliderLink(String sliderLink) {
        this.sliderLink = sliderLink;
    }
}
