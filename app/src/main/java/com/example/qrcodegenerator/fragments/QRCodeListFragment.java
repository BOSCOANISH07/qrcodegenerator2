package com.example.qrcodegenerator.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.activities.QrDetailActivity;
import com.example.qrcodegenerator.adapters.QRCodeAdapter;
import com.example.qrcodegenerator.models.QRCodeItem;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class QRCodeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private QRCodeAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_list, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        Query query = db.collection("qrcodes").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<QRCodeItem> options = new FirestoreRecyclerOptions.Builder<QRCodeItem>()
                .setQuery(query, QRCodeItem.class)
                .build();

        adapter = new QRCodeAdapter(options);
        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            QRCodeItem qrCodeItem = documentSnapshot.toObject(QRCodeItem.class);
            Intent intent = new Intent(getActivity(), QrDetailActivity.class);
            intent.putExtra("qrData", qrCodeItem.getData());
            intent.putExtra("qrType", qrCodeItem.getType());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}