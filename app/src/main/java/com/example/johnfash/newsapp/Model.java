package com.example.johnfash.newsapp;

public class Model {
    String mNewsSection;
    String mNewsTitle;
    String mNewsDate;
    String mNewsUrl;
    String mNiceDate;
    String mNewsAuthor;

    public Model(String newsSection, String newsTitle, String newsDate, String niceDate, String newsUrl, String newsAuthor) {
        mNewsSection = newsSection;
        mNewsTitle = newsTitle;
        mNewsDate = newsDate;
        mNewsUrl = newsUrl;
        mNiceDate = niceDate;
        mNewsAuthor = newsAuthor;
    }

    public String getNewSection() {
        return mNewsSection;
    }

    public String getNewsTitle() {
        return mNewsTitle;
    }

    public String getNewsDate() {
        return mNewsDate;
    }

    public String getNewsUrl() {
        return mNewsUrl;
    }

    public String getNiceDate() {
        return mNiceDate;
    }

    public String getNewsAuthor() {
        return mNewsAuthor;
    }


}
