package com.example.qrcodegenerator.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.utils.QRGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentQRActivity extends AppCompatActivity {

    private static final String TAG = "PaymentQRActivity";

    private Button btnGenerate, btnSave, btnShare;
    private ImageView ivQRCode;
    private TextView tvPaymentLink;
    private Bitmap qrBitmap;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView etUpiId, etName, etAmount, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_qr);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnGenerate = findViewById(R.id.btnGenerate);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvPaymentLink = findViewById(R.id.tvPaymentLink);

        etUpiId = findViewById(R.id.etUpiId);
        etName = findViewById(R.id.etName);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
    }

    private void setupClickListeners() {
        btnGenerate.setOnClickListener(v -> generatePaymentQR());
        btnSave.setOnClickListener(v -> saveQRCode());
        btnShare.setOnClickListener(v -> shareQRCode());
    }

    private void generatePaymentQR() {
        try {
            String upiId = etUpiId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (upiId.isEmpty()) {
                Toast.makeText(this, "Please enter UPI ID", Toast.LENGTH_SHORT).show();
                return;
            }

            String paymentString = buildPaymentString(upiId, name, amount, description);
            generateAndDisplayQR(paymentString);
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR: ", e);
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildPaymentString(String upiId, String name, String amount, String description) {
        StringBuilder builder = new StringBuilder("upi://pay?pa=").append(upiId);
        if (!name.isEmpty()) builder.append("&pn=").append(name);
        if (!amount.isEmpty()) builder.append("&am=").append(amount);
        if (!description.isEmpty()) builder.append("&tn=").append(description);
        return builder.toString();
    }

    private void generateAndDisplayQR(String paymentString) {
        qrBitmap = QRGenerator.generateQRCode(paymentString, 800, 800);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            ivQRCode.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            tvPaymentLink.setVisibility(View.VISIBLE);
            tvPaymentLink.setText("Payment Link: " + paymentString);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "PaymentQR_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";

            // Save to gallery
            Uri imageUri = saveToGallery(fileName);
            if (imageUri != null) {
                Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();

                // Save to cache and upload to Cloudinary
                File imageFile = saveBitmapToFile(qrBitmap, fileName);
                if (imageFile != null) {
                    uploadToCloudinary(imageFile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR code: ", e);
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri saveToGallery(String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PaymentQR");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            OutputStream outStream = getContentResolver().openOutputStream(uri);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        }
        return uri;
    }

    private File saveBitmapToFile(Bitmap bitmap, String fileName) {
        File file = null;
        FileOutputStream out = null;
        try {
            file = new File(getCacheDir(), fileName);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file;
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to file: ", e);
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing stream: ", e);
            }
        }
    }

    private void uploadToCloudinary(File imageFile) {
        new Thread(() -> {
            try {
                // Note: In production, move these credentials to a secure location
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dzkloj6gn",
                        "api_key", "-D-Xv2kXxENjzf7OgQqgjwHXKKs",
                        "api_secret", "127772957645839"));

                Map uploadResult = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");
                saveToRealtimeDB(imageUrl);

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cloudinary upload failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Cloudinary upload error: ", e);
                });
            }
        }).start();
    }

    private void saveToRealtimeDB(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        String paymentString = tvPaymentLink.getText().toString().replace("Payment Link: ", "");

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("qrCodes");
        String qrId = database.push().getKey();

        Map<String, Object> qrData = new HashMap<>();
        qrData.put("userId", userId);
        qrData.put("type", "payment");
        qrData.put("data", paymentString);
        qrData.put("imageUrl", imageUrl);
        qrData.put("timestamp", System.currentTimeMillis());

        database.child(qrId).setValue(qrData)
                .addOnSuccessListener(aVoid -> runOnUiThread(() ->
                        Toast.makeText(this, "Saved to Realtime Database", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(this, "Realtime DB save failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Database save error: ", e);
                }));
    }

    private void shareQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "shared_qr_" + System.currentTimeMillis() + ".png";
            Uri imageUri = createShareableImageUri(fileName);

            if (imageUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share QR Code via"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing QR code: ", e);
            Toast.makeText(this, "Failed to share QR", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createShareableImageUri(String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SharedQR");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri != null) {
            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        }
        return imageUri;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrBitmap != null && !qrBitmap.isRecycled()) {
            qrBitmap.recycle();
            qrBitmap = null;
        }
    }
}