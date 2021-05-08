package com.cu.newsodroid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cu.newsodroid.R;
import com.cu.newsodroid.model.articles;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class news_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater inflater;
    List<articles> articles=new ArrayList<>();
    private OnItemClickListener mListener;
    private static final int item_data=0;
    private static final int item_banner=1;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onImageClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public news_adapter(Context context, List<articles> articles){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.articles=articles;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType){
            case 1:{
                View v = inflater.inflate(R.layout.single_news_card, parent, false);
                viewHolder = new MyHolder(v,mListener);
                break;
            }
            case 2:{
                View v = inflater.inflate(R.layout.banner_single, parent, false);
                viewHolder = new ViewHolderAdMob(v);
                break;
            }
        }
        return viewHolder;

    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        articles model = articles.get(holder.getAdapterPosition());

        switch(holder.getItemViewType()){
            case 1:{
                MyHolder myHolder = (MyHolder) holder;

                myHolder.title.setText(model.getTitle());
                String day=model.getPublishedAt().substring(8,10);
                String month=model.getPublishedAt().substring(5,7);
                String year=model.getPublishedAt().substring(0,4);
                String hour=model.getPublishedAt().substring(11,13);
                String min=model.getPublishedAt().substring(14,16);
                String sec=model.getPublishedAt().substring(17,19);
                String date= day+"/"+month+"/"+year+" "+hour+":"+min+":"+sec;

                DateFormat utcFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                utcFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                DateFormat indianFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                utcFormat.setTimeZone(TimeZone.getTimeZone("IST"));
                Date timestamp = null;
                try {
                    timestamp = utcFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String istTime = indianFormat.format(timestamp);

                try {
                    Date datea=indianFormat.parse(istTime);

                    myHolder.date_tv.setText(TimeAgo.using(datea.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Glide.with(context)
                        .load(model.getUrlToImage())
                        .placeholder(R.drawable.image)
                        .into(myHolder.news_img);
                break;
            }
            case 2:{
                break;
            }
        }
    }




    @Override
    public int getItemCount() {
        return articles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return articles.get(position).getViewType();
    }


    class MyHolder extends RecyclerView.ViewHolder {

        ImageView news_img;
        TextView title,date_tv;


        public MyHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            news_img=itemView.findViewById(R.id.news_img);
            date_tv=itemView.findViewById(R.id.date);
            title=itemView.findViewById(R.id.title);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

//            news_img.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (listener != null) {
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            listener.onImageClick(position);
//                        }
//                    }
//                }
//            });

        }
    }

    public static class ViewHolderAdMob extends RecyclerView.ViewHolder {
        public AdView mAdView;
        public ViewHolderAdMob(View view) {
            super(view);
            View adContainer = view.findViewById(R.id.adMobView);

            AdView mAdView = new AdView(view.getContext());
            AdSize adSize = new AdSize(AdSize.FULL_WIDTH, 136);
            mAdView.setAdSize(adSize);
            mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
            ((RelativeLayout)adContainer).addView(mAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }



}
