package com.cu.newsodroid.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.cu.newsodroid.R;
import com.cu.newsodroid.adapters.news_adapter;
import com.cu.newsodroid.config.APIInterface;
import com.cu.newsodroid.config.APiUtils;
import com.cu.newsodroid.config.static_variables;
import com.cu.newsodroid.model.articles;
import com.cu.newsodroid.model.error_resp;
import com.cu.newsodroid.model.news_resp;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onesignal.OneSignal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class Home extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "56d066bb-515a-4af5-9358-a4518125afd9";
    APIInterface apiInterface;
    ProgressDialog pd;
    Boolean show_ads=true;
    String country_ ="";
    RecyclerView recyclerView;
    FirebaseRemoteConfig fbRemoteConfig;
    SwipeRefreshLayout swipeRefreshLayout;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayoutManager linearLayoutManager;
    news_adapter news_adapter;
    public static final int REQUEST_CHECK_SETTINGS=22;
    private FusedLocationProviderClient fusedLocationClient;
    List<articles> articlesList=new ArrayList<>();
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        String current_country = "";
        shimmerFrameLayout=findViewById(R.id.shimmer);
        shimmerFrameLayout.setVisibility(View.GONE);
        apiInterface = APiUtils.apiInterface();
        recyclerView = findViewById(R.id.recycle);
        //well this is a comment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Boolean location_use = getIntent().getBooleanExtra("location", false);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        swipeRefreshLayout=findViewById(R.id.swipe_referesh_home);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                articlesList.clear();
                news_adapter.notifyDataSetChanged();
                shimmerFrameLayout=findViewById(R.id.shimmer);
                shimmerFrameLayout.setVisibility(View.GONE);
                news_request(country_);
            }
        });

        pd=new ProgressDialog(this);
        pd.setMessage("getting location...");
        if (location_use) {
            //turn on location and use current lcoation
            statusCheck();
        } else {
            current_country = getIntent().getStringExtra("country");
            news_request(current_country);
        }

        fbRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        fbRemoteConfig.setConfigSettingsAsync(configSettings);

        fbRemoteConfig.fetch(0).addOnCompleteListener(this,
                new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            fbRemoteConfig.activate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
                                    if(task.isSuccessful()){
                                        //Toast.makeText(Home.this, fbRemoteConfig.getString("show_ads"), Toast.LENGTH_SHORT).show();
                                        show_ads=fbRemoteConfig.getBoolean("show_ads");
                                    }

                                    else{
                                        Toast.makeText(Home.this, "error in config", Toast.LENGTH_SHORT).show();
                                    }
                                     }
                            });

                        } else {
                            Toast.makeText(Home.this, "error in config '", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void news_request(String country) {
        country_ =country;
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        try {

            Call<news_resp> call = apiInterface.getUsers(country, static_variables.apiKey);

            call.enqueue(new Callback<news_resp>() {
                @Override
                public void onResponse(Call<news_resp> call, Response<news_resp> response) {

                    if (response.code() == 200) {

                        for(int i=0;i<response.body().getArticles().size();i++){
                            articles article=response.body().getArticles().get(i);
                            article.setViewType(1);
                            articlesList.add(article);
                        }

                        if(show_ads){
                            for(int i=2;i<response.body().getArticles().size();i+=3){
                                articles article=new articles();
                                article.setViewType(2);
                                articlesList.add(i,article);
                            }
                        }

                        recycle_init(articlesList);

                        //  Toast.makeText(Home.this, response.body().getArticles().get(0).getTitle(), Toast.LENGTH_SHORT).show();
                    } else {
                        Gson gson = new GsonBuilder().create();
                        error_resp error_resp = new error_resp();
                        try {
                            error_resp = gson.fromJson(response.errorBody().string(), error_resp.class);
                            Toast.makeText(Home.this, error_resp.getMessage(), Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            Toast.makeText(Home.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<news_resp> call, Throwable t) {
                    Toast.makeText(Home.this, t.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void recycle_init(List<articles> articles) {
        swipeRefreshLayout.setRefreshing(false);
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        news_adapter = new news_adapter(this, articles);
        recyclerView.setAdapter(news_adapter);

        news_adapter.setOnItemClickListener(new news_adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(Home.this, WebView_news.class);
                intent.putExtra("url", articles.get(position).getUrl());
                intent.putExtra("heading", articles.get(position).getSource().getName());
                startActivity(intent);



            }

            @Override
            public void onImageClick(int position) {
                Toast.makeText(Home.this, "Image clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //turn on location and get location
            displayLocationSettingsRequest(Home.this);
        } else {
            //get location
            get_location();
        }
    }

    public void get_location(){
        pd.show();
        @SuppressLint("MissingPermission")
        Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation( PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken());
        currentLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location=task.getResult();
                    try {
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if(addresses != null && addresses.size() > 0) {
                            //Toast.makeText(Home.this, addresses.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
                            System.out.println( addresses.get(0).getCountryName());
                            pd.dismiss();
                            news_request(country_string(addresses.get(0).getCountryName()));

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(Home.this, "couldn't get location", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        String TAG="";
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                            Toast.makeText(Home.this, "Location is off", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        Toast.makeText(Home.this, "Location is off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String TAG = "";
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");


                        get_location();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        //Toast.makeText(this, "Location is off!", Toast.LENGTH_SHORT).show();
                        show_dialog();
                        break;
                }
                break;
        }
    }

    public void show_dialog(){
        final Dialog dialog = new Dialog(Home.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.location_dialog);
        dialog.show();
        Button ind_btn=dialog.findViewById(R.id.ind_btn);
        Button us_btn=dialog.findViewById(R.id.us_btn);

        ind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                news_request(static_variables.India);
                dialog.dismiss();
            }
        });

        us_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                news_request(static_variables.United_States);
                dialog.dismiss();
            }
        });

    }
    public String country_string(String country){
        String return_str="";
        switch (country){
            case "Argentina":return_str=static_variables.Argentina;
                break;
            case "Australia":return_str=static_variables.Australia;
                break;
            case "Austria":return_str=static_variables.Austria;
                break;
            case "Belgium":return_str=static_variables.Belgium;
                break;
            case "Brazil":return_str=static_variables.Brazil;
                break;
            case "Bulgaria":return_str=static_variables.Bulgaria;
                break;
            case "Canada":return_str=static_variables.Canada;
                break;
            case "China":return_str=static_variables.China;
                break;
            case "Colombia":return_str=static_variables.Colombia;
                break;
            case "Cuba":return_str=static_variables.Cuba;
                break;
            case "Czech Republic":return_str=static_variables.Czech_Republic;
                break;
            case "Egypt":return_str=static_variables.Egypt;
                break;
            case "France":return_str=static_variables.France;
                break;
            case "Germany":return_str=static_variables.Germany;
                break;
            case "Greece":return_str=static_variables.Greece;
                break;
            case "Hong Kong":return_str=static_variables.Hong_Kong;
                break;
            case "Hungary":return_str=static_variables.Hungary;
                break;
            case "India":return_str=static_variables.India;
                break;
            case "Indonesia":return_str=static_variables.Indonesia;
                break;
            case "Ireland":return_str=static_variables.Ireland;
                break;
            case "Israel":return_str=static_variables.Israel;
                break;
            case "Italy":return_str=static_variables.Italy;
                break;
            case "Japan":return_str=static_variables.Japan;
                break;
            case "Latvia":return_str=static_variables.Latvia;
                break;
            case "Lithuania":return_str=static_variables.Lithuania;
                break;
            case "Malaysia":return_str=static_variables.Malaysia;
                break;
            case "Mexico":return_str=static_variables.Mexico;
                break;
            case "Morocco":return_str=static_variables.Morocco;
                break;
            case "Netherlands":return_str=static_variables.Netherlands;
                break;
            case "New Zealand":return_str=static_variables.New_Zealand;
                break;
            case "Nigeria":return_str=static_variables.Nigeria;
                break;
            case "Norway":return_str=static_variables.Norway;
                break;
            case "Philippines":return_str=static_variables.Philippines;
                break;
            case "Poland":return_str=static_variables.Poland;
                break;
            case "Portugal":return_str=static_variables.Portugal;
                break;
            case "Romania":return_str=static_variables.Romania;
                break;
            case "Russia":return_str=static_variables.Russia;
                break;
            case "Saudi Arabia":return_str=static_variables.Saudi_Arabia;
                break;
            case "Serbia":return_str=static_variables.Serbia;
                break;
            case "Singapore":return_str=static_variables.Singapore;
                break;
            case "Slovakiask":return_str=static_variables.Slovakia;
                break;
            case "Slovenia":return_str=static_variables.Slovenia;
                break;
            case "South Africa":return_str=static_variables.South_Africa;
                break;
            case "South Korea":return_str=static_variables.South_Korea;
                break;
            case "Sweden":return_str=static_variables.Sweden;
                break;
            case "Switzerland":return_str=static_variables.Switzerland;
                break;
            case "Taiwan":return_str=static_variables.Taiwan;
                break;
            case "Thailand":return_str=static_variables.Thailand;
                break;
            case "Turkey":return_str=static_variables.Turkey;
                break;
            case "United Arab Emirates":return_str=static_variables.UAE;
                break;
            case "Ukraine":return_str=static_variables.Ukraine;
                break;
            case "United Kingdom":return_str=static_variables.United_Kingdom;
                break; 
            case "United States":return_str=static_variables.United_States;
                break;
            case "Venuzuela":return_str=static_variables.Venuzuela;
                break;
            default:return_str=static_variables.India;

        }

        return  return_str;
    }

}