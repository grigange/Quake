package com.example.myapplication;


import com.example.myapplication.model.UsgsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface IApi {
    
    //@GET("fdsnws/event/1/query?format=geojson&latitude=40.5&longitude=22&maxradius=5&limit=10")
    @GET("fdsnws/event/1/query")
    Call<UsgsModel> getData(
            @Query("format") String format,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("maxradius") int maxradius,
            @Query("limit") int limit
    );
    @GET("fdsnws/event/1/query")
    Call<UsgsModel> getData(
            @Query("format") String format,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("maxradius") int maxradius,
            @Query("starttime") String starttime,
            @Query("limit") int limit

    );
}
