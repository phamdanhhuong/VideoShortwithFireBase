package com.phamhuong.videoshortwithfirebase.Adpater;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phamhuong.videoshortwithfirebase.Model.Video1Model;
import com.phamhuong.videoshortwithfirebase.R;

public class VideoFireBaseAdapter extends FirebaseRecyclerAdapter<Video1Model, VideoFireBaseAdapter.MyHolder> {

    public VideoFireBaseAdapter(@NonNull FirebaseRecyclerOptions<Video1Model> options) {
        super(options);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Implementation for creating the ViewHolder will go here
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_video_row, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private VideoView videoView;
        private ProgressBar videoProgressBar;
        private TextView textVideoTitle;
        private TextView textVideoDescription;
        private TextView textVideoEmail;
        private TextView txtLikeCount;
        private TextView txtDislikeCount;
        private ImageView imPerson, favorites, imShare, imMore, dislike;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            imPerson = itemView.findViewById(R.id.imPerson);
            favorites = itemView.findViewById(R.id.favorites);
            imShare = itemView.findViewById(R.id.imShare);
            imMore = itemView.findViewById(R.id.imMore);
            textVideoEmail = itemView.findViewById(R.id.tvEmailVideo);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtDislikeCount = itemView.findViewById(R.id.txtDislikeCount);
            dislike = itemView.findViewById(R.id.dislike);
        }
    }
    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Video1Model model) {
        //Video1Model videoModel = videoList.get(position); // Assuming videoList is not used here as data comes from Firebase
        holder.textVideoTitle.setText(model.getTitle());
        holder.textVideoDescription.setText(model.getDesc());
        holder.textVideoEmail.setText(model.getEmail());
        //holder.videoView.setVideoPath(model.getVideoUrl());
        holder.videoView.setVideoURI(Uri.parse(model.getUrl()));
        holder.txtLikeCount.setText(String.valueOf(model.getLike()));
        holder.txtDislikeCount.setText(String.valueOf(model.getDislike()));

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(model.getUid());
        userRef.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    // Dùng avatarUrl để load ảnh, ví dụ với Glide:
                    Glide.with(holder.imPerson.getContext()).load(avatarUrl).into(holder.imPerson);
                } else {
                    Glide.with(holder.imPerson.getContext()).load(R.drawable.ic_person_pin).into(holder.imPerson);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                
            }
        });

        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.videoProgressBar.setVisibility(View.GONE);
                mp.start();

                float videoRatio = (float) mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = (float) holder.videoView.getWidth() / (float) holder.videoView.getHeight();
                float scale = videoRatio / screenRatio;

                if (scale >= 1f) {
                    holder.videoView.setScaleX(scale);
                } else {
                    holder.videoView.setScaleY(1f / scale);
                }
            }
        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        holder.favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference videoRef = getRef(position);
                int newLike = model.getLike() + 1;
                videoRef.child("like").setValue(newLike);
            }
        });

        holder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference videoRef = getRef(position);
                int newDislike = model.getDislike() + 1;
                videoRef.child("dislike").setValue(newDislike);
            }
        });
    }
}
