package com.cu.newsodroid.config;

import java.util.List;
import android.content.SharedPreferences;

import com.cu.newsodroid.model.news_resp;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface APIInterface {


    @GET("/v2/top-headlines")
    public Call<news_resp> getUsers(@Query("country") String country,
                                    @Query("apiKey") String apiKey);

}


