package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.model.Feature;
import com.example.myapplication.model.UsgsModel;
import com.example.myapplication.model.WarningModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WarningService extends Service {
    private static final String CHANNEL_ID = "12345";
    NotificationChannel channel;

    private LocationManager locationManager;
    public static Location currentLocation;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do something with the location, such as send it to the API
            currentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.warnings);
            String description = getString(R.string.earthquake_warnings);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (!currentLocation.equals(null)){
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
            String str = formatter.format(date);

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("id",Context.MODE_PRIVATE);

            createNotificationChannel();
            Intent intent = new Intent(getApplicationContext(),WarningActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);


            FirebaseFirestore db = FirebaseFirestore.getInstance();


            Retrofit retrofit = new Retrofit
                    .Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://earthquake.usgs.gov/").build();

            IApi api = retrofit.create(IApi.class);

            Call<UsgsModel> call = api.getData("geojson",currentLocation==null?0:currentLocation.getLatitude(),currentLocation==null?0:currentLocation.getLongitude(),1, str,10);
            call.enqueue(new Callback<UsgsModel>() {
                @Override
                public void onResponse(Call<UsgsModel> call, Response<UsgsModel> response) {

                    UsgsModel model = response.body();
                    ArrayList<Feature> features = model.getFeatures();
                    if (features.isEmpty()){ return;}
                    for (Feature feature : features) {

                        CollectionReference collRef = db.collection("warnings");
                        Query q = collRef.whereEqualTo("id",sharedPref.getString("id","id")).whereEqualTo("title",feature.getProperties().getPlace());
                        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if(task.getResult().getDocuments().isEmpty()) {

                                            db.collection("warnings").add(new WarningModel(
                                                    sharedPref.getString("id", "id"),
                                                    feature.getProperties().getPlace(),
                                                    new Timestamp(new Date(feature.getProperties().getTime())),
                                                    feature.getProperties().getMag(), new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                                    new GeoPoint(feature.getGeometry().getCoordinates().get(1), feature.getGeometry().getCoordinates().get(0)))
                                            );
                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                    .setSmallIcon(R.drawable.ic_earthquake_square)
                                                    .setContentTitle("Earthquake near you")
                                                    .setContentText(feature.getProperties().getPlace())
                                                    .setContentIntent(pendingIntent)
                                                    .setStyle(new NotificationCompat.BigTextStyle()
                                                            .bigText(feature.getProperties().getPlace() +
                                                                    "\nRichter: " + feature.getProperties().getMag() +
                                                                    "\nLatitude: " + feature.getGeometry().getCoordinates().get(1) +
                                                                    "\nLongitude: " + feature.getGeometry().getCoordinates().get(0)))
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH);
                                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                            notificationManager.notify(1, builder.build());
                                    }
                                }


                            }
                        });


                    }


                }


                @Override
                public void onFailure(Call<UsgsModel> call, @NonNull Throwable t) {
                    t.fillInStackTrace();


                }
            });
        }}
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.scheduleAtFixedRate(timerTask, 0, 30000); // Schedule the timer to run every minute
        return START_STICKY;
    }
    public WarningService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

}