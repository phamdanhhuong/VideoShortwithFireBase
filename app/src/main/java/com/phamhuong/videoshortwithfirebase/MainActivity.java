package com.phamhuong.videoshortwithfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.phamhuong.videoshortwithfirebase.Adpater.VideoFireBaseAdapter;
import com.phamhuong.videoshortwithfirebase.Model.Video1Model;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideoFireBaseAdapter videosAdapter;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager2 = findViewById(R.id.vpager);
        initializeFirebase();
        getVideos();


        ImageButton btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeFirebase() {
        try {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("videos");
            mDatabaseReference.keepSynced(true); // Enable offline persistence
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getVideos() {
        try {
            FirebaseRecyclerOptions<Video1Model> options =
                    new FirebaseRecyclerOptions.Builder<Video1Model>()
                            .setQuery(mDatabaseReference, Video1Model.class)
                            .build();
            
            videosAdapter = new VideoFireBaseAdapter(options);
            viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
            viewPager2.setAdapter(videosAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading videos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        videosAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videosAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videosAdapter.notifyDataSetChanged();
    }
}