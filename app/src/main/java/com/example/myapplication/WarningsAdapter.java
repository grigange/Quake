package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.WarningModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WarningsAdapter extends RecyclerView.Adapter<WarningsAdapter.ViewHolder> {

    private Context mContext;
    private List<WarningModel> mData;

    public WarningsAdapter(Context mContext, List<WarningModel> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }


    //Class that holds the items to be displayed (Views in card_layout)
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemDetail;
        CardView itemCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_date);
            itemCard = itemView.findViewById(R.id.card_view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.warning_layout, parent, false);

        return new ViewHolder(v);

    }


    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_out_row);
        holder.itemView.startAnimation(animation);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_row);
        holder.itemTitle.setText(mData.get(position).getTitle()==null?"Earthquake":mData.get(position).getTitle());
        Date date = new Date(mData.get(position).getTime().getSeconds()*1000);
        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.getDefault());
        holder.itemDetail.setText(mData.get(position).getMag() + " • " + df.format(date));
        holder.itemView.startAnimation(animation);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
