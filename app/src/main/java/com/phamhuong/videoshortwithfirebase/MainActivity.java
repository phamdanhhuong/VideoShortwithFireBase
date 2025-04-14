package com.phamhuong.videoshortwithfirebase;

import android.os.Bundle;

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
        getVideos();
    }
    private void getVideos() {
        //**set database*/
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("videos");
        FirebaseRecyclerOptions<Video1Model> options =
                new FirebaseRecyclerOptions.Builder<Video1Model>()
                        .setQuery(mDatabaseReference, Video1Model.class)
                        .build();
        //**set adapter*/
        videosAdapter = new VideoFireBaseAdapter(options);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPager2.setAdapter(videosAdapter);
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