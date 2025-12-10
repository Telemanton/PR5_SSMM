package com.example.pr7_ssmm;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{


    private LocationManager locManager;
    private GoogleMap mMap;

    private final LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();


            Log.i("Location", "Lat: " + lat + ", Lon: " + lon);

            if (mMap != null) {
                LatLng current = new LatLng(lat, lon);
                mMap.clear(); // Remove old markers
                mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

    }

    protected void onResume() {
        super.onResume();

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para usar GPS.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    225);
        } else {
            Log.i("Mensaje", "Se tiene permiso para usar GPS!");
            locManager = (LocationManager) getSystemService(Context.
                    LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1.0f,locationListenerGPS);
        }

    }



            @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }


}








