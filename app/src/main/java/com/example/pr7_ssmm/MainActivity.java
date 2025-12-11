package com.example.pr7_ssmm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // GPS y Mapa
    private GoogleMap mMap;
    private LocationManager locManager;
    private boolean gpsActivo = false;
    private final LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            if (mMap != null) {
                LatLng current = new LatLng(lat, lon);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(current).title("Posición actual"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
            }
        }
    };

    // FIREBASE (exacto de la práctica)
    FirebaseFirestore db;
    StorageReference storageRef;
    private String email = "test_user"; // Simulado para pruebas
    private EditText direccionEditText, telefonoEditText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  INICIALIZACIÓN MANUAL COMPLETA (TU google-services.json)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBxd0qCFnQctxiY8j2UqsRF4TV4laLxBwc")
                    .setApplicationId("1:180927506307:android:f58d323b5dd4fd6b3978b6")
                    .setProjectId("p5-ssmm-f41aa")
                    .setStorageBucket("p5-ssmm-f41aa.appspot.com")  // ← BUCKET AÑADIDO
                    .build();
            FirebaseApp.initializeApp(this, options);
        }

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // INICIALIZAR VISTAS FIREBASE
        direccionEditText = findViewById(R.id.direccionEditText);
        telefonoEditText = findViewById(R.id.telefonoEditText);


        // BOTÓN GPS
        FloatingActionButton btnLocation = findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(v -> toggleGPS());

        // BOTONES CRUD
        setupFirebaseButtons();
    }

    // Botones CRUD
    private void setupFirebaseButtons() {
        // Botón GUARDAR
        Button guardarButton = (Button) findViewById(R.id.guardarButton);
        guardarButton.setOnClickListener(view -> {
            // Obtener datos de campos
            Map<String, Object> data = new HashMap<>();
            data.put("address", direccionEditText.getText().toString());
            data.put("phone", telefonoEditText.getText().toString());
            db.collection("users").document(email).set(data);
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
        });

        // Botón Nueva (Create)
        Button nuevaButton = (Button) findViewById(R.id.nuevaButtom);

        /* Create sin comprobación
        nuevaButton.setOnClickListener(view -> {
            // CREAR data AQUÍ cada vez
            Map<String, Object> data = new HashMap<>();
            data.put("address", direccionEditText.getText().toString());
            data.put("phone", telefonoEditText.getText().toString());

            String docId = telefonoEditText.getText().toString(); // opcional , si queremos que el Id sea el teléfono
            db.collection("users").add(data);  // o .document(docId).set(data)
            Toast.makeText(this, "Nuevo registro creado", Toast.LENGTH_SHORT).show();

        });*/

        // Create , con comprobación
        nuevaButton.setOnClickListener(view -> {
            String telefono = telefonoEditText.getText().toString();
            if (telefono.isEmpty()) {
                Toast.makeText(this, "Introduce teléfono", Toast.LENGTH_SHORT).show();
                return;
            }

            // BUSCAR si teléfono existe
            db.collection("users")
                    .whereEqualTo("phone", telefono)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                // NO existe → CREAR nuevo
                                Map<String, Object> data = new HashMap<>();
                                data.put("address", direccionEditText.getText().toString());
                                data.put("phone", telefono);
                                db.collection("users").add(data);
                                Toast.makeText(this, "Nuevo registro creado", Toast.LENGTH_SHORT).show();
                            } else {
                                // YA existe
                                Toast.makeText(this, "Teléfono YA existe", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Error consulta", Toast.LENGTH_SHORT).show();
                        }
                    });
        });






        // Botón RECUPERAR (Read)
        Button recuperarButton = (Button) findViewById(R.id.recuperarButton);
        recuperarButton.setOnClickListener(view -> {
            // Recuperar datos Firestore
            db.collection("users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            direccionEditText.setText(data.get("address").toString());
                            telefonoEditText.setText(data.get("phone").toString());
                        }
                    });

            // Recuperar imagen Storage (opcional)
            StorageReference islandRef = storageRef.child("prueba.png");
            final long ONE_MEGABYTE = 1024 * 1024;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(e -> Log.e("Storage", e.getMessage()));
        });

        // Botón BORRAR
        Button eliminarButton = (Button) findViewById(R.id.eliminarButton);
        eliminarButton.setOnClickListener(view -> {
            db.collection("users").document(email).delete();
            direccionEditText.setText("");
            telefonoEditText.setText("");
            Toast.makeText(this, "Borrado", Toast.LENGTH_SHORT).show();
        });
    }

    // MÉTODOS GPS
    private void toggleGPS() {
        if (!gpsActivo) iniciarGPS(); else detenerGPS();
    }

    private void iniciarGPS() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 225);
            return;
        }
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1.0f, locationListenerGPS);
        gpsActivo = true;
        Toast.makeText(this, "GPS activado", Toast.LENGTH_SHORT).show();
    }

    private void detenerGPS() {
        if (locManager != null) locManager.removeUpdates(locationListenerGPS);
        gpsActivo = false;
        Toast.makeText(this, "GPS desactivado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 225 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarGPS();
        }
    }

    @Override
    protected void onResume() { super.onResume(); if (gpsActivo) iniciarGPS(); }
    @Override
    protected void onPause() { super.onPause(); if (gpsActivo) detenerGPS(); }
}
