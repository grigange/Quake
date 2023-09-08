package com.example.myapplication.ui.infoList;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentListBinding;
import com.example.myapplication.IApi;
import com.example.myapplication.RecyclerAdapter;
import com.example.myapplication.model.Feature;
import com.example.myapplication.model.UsgsModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    LatLng myLocation = new LatLng(0, 0);
    private FusedLocationProviderClient fusedLocationClient;

    private void PutDataIntoRecyclerView(List<Feature> data) {
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), data);
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);


    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null){
                        myLocation = new LatLng(task.getResult().getLatitude(),task.getResult().getLongitude());
                        Retrofit retrofit = new Retrofit
                                .Builder()
                                .addConverterFactory(GsonConverterFactory.create())
                                .baseUrl("https://earthquake.usgs.gov/").build();

                        IApi api = retrofit.create(IApi.class);
                        Call<UsgsModel> call = api.getData("geojson", myLocation.latitude, myLocation.longitude, 5,10);
                        call.enqueue(new Callback<UsgsModel>() {
                            @Override
                            public void onResponse(Call<UsgsModel> call, Response<UsgsModel> response) {
                                UsgsModel model = response.body();
                                ArrayList<Feature> features = model.getFeatures();
                                PutDataIntoRecyclerView(features);
                                if (features.isEmpty()){
                                    binding.emptyView.setVisibility(View.VISIBLE);
                                    binding.recycler.setVisibility(View.GONE);}
                            }

                            @Override
                            public void onFailure(Call<UsgsModel> call, @NonNull Throwable t) {
                                t.fillInStackTrace();


                            }
                        });

                    }
                }
            }
        });




        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}