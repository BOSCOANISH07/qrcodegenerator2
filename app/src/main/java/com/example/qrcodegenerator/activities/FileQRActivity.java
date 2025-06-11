package com.example.qrcodegenerator.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrcodegenerator.R;
import com.example.qrcodegenerator.utils.QRGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileQRActivity extends AppCompatActivity {

    private static final int FILE_PICK_CODE = 1001;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String TAG = "FileQRActivity";
    private static final long MAX_FILE_SIZE = 100_000_000; // 100MB

    private Button btnSelectFile, btnGenerate, btnSave, btnUpload;
    private TextView tvSelectedFile;
    private ImageView ivQRCode;
    private Uri fileUri;
    private String cloudinaryUrl;
    private Bitmap qrBitmap;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    // Cloudinary configuration
    private static final String CLOUD_NAME = "your_cloud_name";
    private static final String API_KEY = "your_api_key";
    private static final String API_SECRET = "your_api_secret";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_qr);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnSave = findViewById(R.id.btnSave);
        btnUpload = findViewById(R.id.btnUpload);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        ivQRCode = findViewById(R.id.ivQRCode);
    }

    private void setupClickListeners() {
        btnSelectFile.setOnClickListener(v -> selectFile());
        btnGenerate.setOnClickListener(v -> generateLocalQR());
        btnSave.setOnClickListener(v -> {
            if (checkPermission()) {
                saveQRCode();
            } else {
                requestPermission();
            }
        });
        btnUpload.setOnClickListener(v -> uploadToCloudinary());
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            tvSelectedFile.setText("Selected: " + fileName);
            btnUpload.setVisibility(View.VISIBLE);
            btnGenerate.setEnabled(true);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "file";
    }

    private void generateLocalQR() {
        if (fileUri != null) {
            String content = "FILE:" + fileUri.toString();
            generateQRCode(content);
        }
    }

    private void generateCloudQR() {
        if (cloudinaryUrl != null) {
            generateQRCode(cloudinaryUrl);
        }
    }

    private void generateQRCode(String content) {
        qrBitmap = QRGenerator.generateQRCode(content, 800, 800);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
            ivQRCode.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToCloudinary() {
        if (fileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            byte[] fileBytes = buffer.toByteArray();

            if (fileBytes.length > MAX_FILE_SIZE) {
                Toast.makeText(this, "File too large (max 100MB)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate Cloudinary signature
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = DigestUtils.sha1Hex("timestamp=" + timestamp + API_SECRET);

            RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(getMimeType(fileUri)));
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", getFileName(fileUri), fileBody)
                    .addFormDataPart("api_key", API_KEY)
                    .addFormDataPart("timestamp", String.valueOf(timestamp))
                    .addFormDataPart("signature", signature)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/auto/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(FileQRActivity.this,
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(FileQRActivity.this,
                                "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        cloudinaryUrl = jsonObject.getString("secure_url");

                        runOnUiThread(() -> {
                            generateCloudQR();
                            saveToFirebase();
                            Toast.makeText(FileQRActivity.this,
                                    "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(FileQRActivity.this,
                                "Error parsing response", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Upload error", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(Uri uri) {
        ContentResolver resolver = getContentResolver();
        String type = resolver.getType(uri);
        return type != null ? type : "application/octet-stream";
    }

    private void saveToFirebase() {
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("url", cloudinaryUrl);
        fileData.put("timestamp", System.currentTimeMillis());
        fileData.put("userId", userId);

        // Save to Realtime Database
        databaseRef.child("cloudinary_files").push().setValue(fileData);

        // Save to Firestore
        db.collection("cloudinary_files").add(fileData)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Firestore document added"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Firestore save failed", e));
    }

    private void saveQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Generate QR code first", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "QR_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/QRCodeGenerator");

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error saving QR code", e);
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveQRCode();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
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