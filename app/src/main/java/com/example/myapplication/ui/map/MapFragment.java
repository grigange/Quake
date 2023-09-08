package com.example.myapplication.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.IApi;
import com.example.myapplication.WarningService;
import com.example.myapplication.model.EarthquakeModel;
import com.example.myapplication.model.Feature;
import com.example.myapplication.model.UsgsModel;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    LatLng myLocation = new LatLng(0, 0);
    LatLng focus = null;
    private FusedLocationProviderClient fusedLocationClient;



    private final static int REQUEST_CODE = 100;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        private GoogleMap mMap;

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            /*mMap.setMaxZoomPreference(14);*/
            mMap.setPadding(0,80,0,0);

            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null){
                            myLocation = new LatLng(task.getResult().getLatitude(),task.getResult().getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(focus==null?myLocation:focus,6,30,5)));
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
                                    for (Feature feature : model.getFeatures()) {
                                        EarthquakeModel eq = new EarthquakeModel(feature.getProperties().getPlace(),
                                                new LatLng(feature.getGeometry().getCoordinates().get(1),
                                                        feature.getGeometry().getCoordinates().get(0)),feature.getProperties().getMag()
                                        );
                                        float s = (70 - 5.5f*eq.getRichter()) > 0? (float) (70 - 5.5f*eq.getRichter()): 0;
                                        CircleOptions circleOptions = new CircleOptions()
                                                .center(eq.getLatlong())
                                                .radius(5000 * eq.getRichter()) // In meters
                                                .strokeWidth(3)
                                                .strokeColor(Color.HSVToColor(255, new float[]{s, 1, 0.70f}))
                                                .fillColor(Color.HSVToColor(155, new float[]{s, 0.90f, 0.90f}));

                                        Date date = new Date(feature.getProperties().getTime());
                                        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale.getDefault());
                                        Circle circle = mMap.addCircle(circleOptions);
                                        Marker marker = mMap.addMarker(new MarkerOptions().position(eq.getLatlong())
                                                .title(eq.getName())
                                                .snippet("Richter: "+eq.getRichter()+"  •  Date: "+df.format(date))
                                                .icon(BitmapDescriptorFactory.defaultMarker((float) (70 - 5.5f*eq.getRichter()))));
                                        if (eq.getLatlong().equals(focus)) {marker.showInfoWindow();}
                                        circle.setTag(eq.getName());
                                    }
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

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    focus = marker.getPosition();
                    return false;
                }
            });
            mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {
                    focus = new LatLng(location.getLatitude(),location.getLongitude());
                }
            });



        }

    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle b = this.getArguments();
        if (b != null) {
            focus = new LatLng(b.getDouble("lat", myLocation.latitude),
                    b.getDouble("long", myLocation.longitude));


        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}