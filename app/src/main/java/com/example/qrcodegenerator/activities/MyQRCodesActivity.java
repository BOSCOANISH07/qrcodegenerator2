package com.example.qrcodegenerator.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.adapters.QRCodeAdapter;
import com.example.qrcodegenerator.models.QRCodeItem;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

public class MyQRCodesActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private QRCodeAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr_codes);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = db.collection("qrcodes")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<QRCodeItem> options = new FirestoreRecyclerOptions.Builder<QRCodeItem>()
                .setQuery(query, QRCodeItem.class)
                .build();

        adapter = new QRCodeAdapter(options);
        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            QRCodeItem qrCodeItem = documentSnapshot.toObject(QRCodeItem.class);
            Intent intent = new Intent(MyQRCodesActivity.this, QrDetailActivity.class);
            intent.putExtra("qrData", qrCodeItem.getData());
            intent.putExtra("qrType", qrCodeItem.getType());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}