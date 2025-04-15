package com.phamhuong.videoshortwithfirebase.Model;

import java.io.Serializable;

public class Video1Model implements Serializable {
    private String title;
    private String desc;
    private String url;
    private String email;
    private String uid;
    private int like;
    private int dislike;

    // Default constructor required for Firebase
    public Video1Model() {
    }

    public Video1Model(String title, String desc, String url, String email, String uid, int like, int dislike) {
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.email = email;
        this.uid = uid;
        this.like = like;
        this.dislike = dislike;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
