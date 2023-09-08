package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.model.Feature;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Feature> mData;

    public RecyclerAdapter(Context mContext, List<Feature> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }


    //Class that holds the items to be displayed (Views in card_layout)
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemMag;
        TextView itemDate;
        Button itemMap;
        Button itemMore;
        CardView itemCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemMag = itemView.findViewById(R.id.item_mag);
            itemDate = itemView.findViewById(R.id.item_date);
            itemMap = itemView.findViewById(R.id.item_map);
            itemMore = itemView.findViewById(R.id.item_more);
            itemCard = itemView.findViewById(R.id.card_view);



        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.card_layout, parent, false);

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
        holder.itemTitle.setText(mData.get(position).getProperties().getPlace()==null?"Earthquake":mData.get(position).getProperties().getPlace());
        Date date = new Date(mData.get(position).getProperties().getTime());
        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.getDefault());
        holder.itemMag.setText("Richter : " +mData.get(position).getProperties().getMag());
        holder.itemDate.setText("Date : " +df.format(date));
        holder.itemMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putDouble("lat",mData.get(position).getGeometry().getCoordinates().get(1));
                b.putDouble("long",mData.get(position).getGeometry().getCoordinates().get(0));
                Navigation.findNavController(view).navigate(R.id.navigation_map,b);
            }
        });
        holder.itemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mData.get(position).getProperties().getUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mContext.startActivity(intent);
            }
        });
        holder.itemView.startAnimation(animation);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
