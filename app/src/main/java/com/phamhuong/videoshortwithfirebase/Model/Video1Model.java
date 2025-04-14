package com.phamhuong.videoshortwithfirebase.Model;

import java.io.Serializable;

public class Video1Model implements Serializable {
    private String title;
    private String desc;
    private String url;

    public Video1Model(String title, String desc, String url) {
        this.title = title;
        this.desc = desc;
        this.url = url;
    }

    public Video1Model() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
