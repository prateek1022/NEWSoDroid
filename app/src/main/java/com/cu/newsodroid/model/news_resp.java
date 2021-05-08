package com.cu.newsodroid.model;

import java.util.List;

public class news_resp {
    String status;
    String totalResults;
    List<articles> articles ;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String totalResults) {
        this.totalResults = totalResults;
    }

    public List<com.cu.newsodroid.model.articles> getArticles() {
        return articles;
    }

    public void setArticles(List<com.cu.newsodroid.model.articles> articles) {
        this.articles = articles;
    }
}
