package com.example.pr7_ssmm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListadoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.listViewUsuarios);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    items.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String address = String.valueOf(doc.get("address"));
                        String phone   = String.valueOf(doc.get("phone"));
                        items.add(phone + " - " + address);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                );
    }
}

