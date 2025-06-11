package com.example.qrcodegenerator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.adapters.DashboardAdapter;

public class StudentDashboardActivity extends AppCompatActivity{

    private GridView gridView;
    private String[] options = {"Text QR", "URL QR", "File QR", "Payment QR", "My QR Codes"};
    private int[] icons = {R.drawable.ic_text, R.drawable.ic_url, R.drawable.ic_file, R.drawable.ic_payment, R.drawable.ic_my_qr};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        gridView = findViewById(R.id.gridView);
        DashboardAdapter adapter = new DashboardAdapter(this, options, icons);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    startActivity(new Intent(StudentDashboardActivity.this, TextQRActivity.class));
                    break;
                case 1:
                    startActivity(new Intent(StudentDashboardActivity.this, UrlQRActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(StudentDashboardActivity.this, FileQRActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(StudentDashboardActivity.this, PaymentQRActivity.class));
                    break;
                case 4:
                    startActivity(new Intent(StudentDashboardActivity.this, MyQRCodesActivity.class));
                    break;
            }
        });
    }
}