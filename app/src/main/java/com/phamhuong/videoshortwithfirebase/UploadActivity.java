package com.phamhuong.videoshortwithfirebase;
import android.Manifest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 100;
    private Uri videoUri;

    Button btnUploadVideo;
    EditText txtTitle, txtDesc;

    Cloudinary cloudinary;
    DatabaseReference firebaseDB;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);

        btnUploadVideo = findViewById(R.id.btnOpenAndUpload);
        txtTitle = findViewById(R.id.txtTitle);
        txtDesc = findViewById(R.id.txtDesc);
        progressDialog = new ProgressDialog(UploadActivity.this);

        // Firebase
        firebaseDB = FirebaseDatabase.getInstance().getReference("videos");

        // Cloudinary config
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dv8sxlyly",
                "api_key", "421839787612154",
                "api_secret", "UYkTyyXAU91n1ixxLoDtMYxV_j0"));

        btnUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickVideo();
                UploadActivity.this.finish();
            }
        });
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn video"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            uploadVideoToCloudinary(videoUri);
        }
    }

    private void uploadVideoToCloudinary(Uri uri) {
        progressDialog.setMessage("Đang upload video...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(() -> {
            try {
                File file = new File(getRealPathFromURI(uri));
                Map uploadResult = cloudinary.uploader().upload(file,
                        ObjectUtils.asMap("resource_type", "video"));

                String videoUrl = (String) uploadResult.get("secure_url");

                saveVideoToFirebase(txtTitle.getText().toString(), txtDesc.getText().toString(), videoUrl);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload thành công", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveVideoToFirebase(String title, String desc, String url) {
        String id = firebaseDB.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> video = new HashMap<>();
        video.put("title", title);
        video.put("desc", desc);
        video.put("url", url);
        if (user != null) {
            video.put("uid", user.getUid());
            video.put("email",user.getEmail());
        }

        if (id != null) {
            firebaseDB.child(id).setValue(video)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FIREBASE", "Video saved: " + url))
                    .addOnFailureListener(e ->
                            Log.e("FIREBASE", "Save failed", e));
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void checkPermissionAndPickVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION);
            } else {
                openVideoPicker();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                openVideoPicker();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVideoPicker();
            } else {
                Toast.makeText(this, "Bạn phải cấp quyền để chọn video", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
