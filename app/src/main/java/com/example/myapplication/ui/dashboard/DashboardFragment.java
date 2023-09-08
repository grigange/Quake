package com.example.myapplication.ui.dashboard;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.WarningService;
import com.example.myapplication.model.WarningModel;
import com.example.myapplication.WarningsAdapter;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private void PutDataIntoWarningsView(List<WarningModel> data) {
        WarningsAdapter adapter = new WarningsAdapter(getContext(), data);
        binding.alerts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.alerts.setAdapter(adapter);


    }

    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WarningService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPref = getContext().getSharedPreferences("id",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        DocumentReference docRef = db.collection("users").document(sharedPref.getString("id","id"));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            binding.switch1.setChecked((Boolean) document.get("service"));
                        } else {
                            binding.switch1.setChecked(false);
                        }
                    } else {
                        Log.d("ERROR", "get failed with ", task.getException());
                    }
                }

        });


        binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b && !isMyServiceRunning(getContext())){
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},100);
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        compoundButton.setChecked(false);
                        return;
                    }
                    Intent intent = new Intent(getContext(), WarningService.class);
                    getActivity().startService(intent);
                }else if (!b && isMyServiceRunning(getContext())){
                    Intent intent = new Intent(getContext(), WarningService.class);
                    getActivity().stopService(intent);
                }
                Map<String, Object> user = new HashMap<>();



                user.put("service",b);
                if(sharedPref.getString("id","id").equals("id")){
                    db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            editor.putString("id", documentReference.getId());
                            editor.apply();
                        }
                    });

                }else{
                    db.collection("users").document(sharedPref.getString("id","id")).update(user);
                }

            }

        });




        ArrayList<WarningModel> warnings = new ArrayList<>();
        db.collection("warnings").whereEqualTo("id", sharedPref.getString("id","id")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                       WarningModel w = new WarningModel("1",
                               (String) document.get("title"),
                               (Timestamp) document.get("time"),
                               (Double) document.get("mag"),
                               document.getGeoPoint("eqLocation"),
                               document.getGeoPoint("myLocation"));
                        warnings.add(w);
                    }
                    if (warnings.isEmpty()){
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.alerts.setVisibility(View.GONE);
                    }

                    PutDataIntoWarningsView(warnings);
                } else {
                    Log.d("error", "Error getting documents: ", task.getException());
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}