package com.cu.newsodroid.config;

public class APiUtils {

  public static final String BASE_URL = "https://newsapi.org";

  public static APIInterface apiInterface(){
    return RetrofitClient.getClient(BASE_URL).create(APIInterface.class);
  }
}