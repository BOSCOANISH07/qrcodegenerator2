package com.example.qrcodegenerator.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.utils.QRGenerator;

public class QrDetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_detail);

        String qrData = getIntent().getStringExtra("qrData");
        String qrType = getIntent().getStringExtra("qrType");

        TextView tvType = findViewById(R.id.tvType);
        TextView tvData = findViewById(R.id.tvData);
        ImageView ivQRCode = findViewById(R.id.ivQRCode);

        tvType.setText("Type: " + qrType);
        tvData.setText("Data: " + qrData);

        Bitmap qrBitmap = QRGenerator.generateQRCode(qrData, 600, 600);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
        }
    }
}