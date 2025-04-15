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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    ImageView imPerson;
    TextView tvEmail, videoCount;
    FirebaseUser user;
    Cloudinary cloudinary;
    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_PICK_IMAGE = 101;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        imPerson = findViewById(R.id.imgAvatar);
        tvEmail = findViewById(R.id.txtEmail);
        videoCount = findViewById(R.id.txtVideoCount);
        progressDialog = new ProgressDialog(ProfileActivity.this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            tvEmail.setText(email);
        }
        countVideo();

        user = FirebaseAuth.getInstance().getCurrentUser();
        // Cloudinary config
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dv8sxlyly",
                "api_key", "421839787612154",
                "api_secret", "UYkTyyXAU91n1ixxLoDtMYxV_j0"));
        if (user != null) {
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(ProfileActivity.this).load(photoUrl).into(imPerson);
            }else {
                Glide.with(ProfileActivity.this).load(R.drawable.ic_person_pin).into(imPerson);
            }
        }

        imPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickImage();
            }
        });
    }


    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            } else {
                openImagePicker();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                openImagePicker();
            }
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String imagePath = getRealPathFromURI(selectedImageUri);
                uploadImageToCloudinary(imagePath);
            }
        }
    }

    private void uploadImageToCloudinary(String imagePath) {
        progressDialog.setMessage("Đang upload avatar...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(() -> {
            try {
                Map uploadResult = cloudinary.uploader().upload(new File(imagePath), ObjectUtils.asMap(
                        "resource_type", "image"
                ));
                String imageUrl = (String) uploadResult.get("secure_url");

                runOnUiThread(() -> {
                    // Cập nhật ảnh lên Firebase Auth
                    updateFirebaseProfilePhoto(imageUrl);
                    // Hiển thị luôn ảnh mới
                    Glide.with(ProfileActivity.this).load(imageUrl).into(imPerson);
                    progressDialog.dismiss();
                });

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Lỗi upload ảnh!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Cập nhật avatar vào Firebase Auth
    private void updateFirebaseProfilePhoto(String photoUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(photoUrl))
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                            userRef.child("avatar").setValue(photoUrl);
                            Toast.makeText(ProfileActivity.this, "Avatar đã cập nhật!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void countVideo() {
        DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference("videos");
        videosRef.orderByChild("uid").equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        videoCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
