package com.example.qrcodegenerator.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.utils.QRGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class UrlQRActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 101;

    private EditText etUrl;
    private Button btnGenerate, btnSave;
    private ImageView ivQRCode;
    private Bitmap qrBitmap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_qr);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etUrl = findViewById(R.id.etUrl);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnSave = findViewById(R.id.btnSave);
        ivQRCode = findViewById(R.id.ivQRCode);

        btnGenerate.setOnClickListener(v -> generateQR());
        btnSave.setOnClickListener(v -> {
            if (checkPermission()) {
                saveQRCode();
            } else {
                requestPermission();
            }
        });
    }

    private void generateQR() {
        String url = etUrl.getText().toString().trim();
        if (url.isEmpty()) {
            etUrl.setError("Please enter URL");
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        qrBitmap = QRGenerator.generateQRCode(url, 800, 800);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            btnSave.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = etUrl.getText().toString().trim();
        String userId = mAuth.getCurrentUser().getUid();

        // Save metadata to Firestore
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("userId", userId);
        qrData.put("type", "url");
        qrData.put("data", url);
        qrData.put("timestamp", System.currentTimeMillis());

        db.collection("qrcodes").add(qrData);

        // Save bitmap to device
        String filename = "QR_" + System.currentTimeMillis() + ".png";
        OutputStream outputStream;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCodeGenerator");

                ContentResolver resolver = getContentResolver();
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri);
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(this, "QR Code saved to Pictures/QRCodeGenerator", Toast.LENGTH_LONG).show();
                }
            } else {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QRCodeGenerator");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, filename);
                outputStream = new FileOutputStream(file);
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(this, "QR Code saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Check for storage permission (required for Android < Q)
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Scoped storage doesn't need permission
        }
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQRCode();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
