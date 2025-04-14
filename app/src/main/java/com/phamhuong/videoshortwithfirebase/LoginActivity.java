package com.phamhuong.videoshortwithfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        EditText emailInput = findViewById(R.id.email);
        EditText passwordInput = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnLogin.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        startActivity(new Intent(this, MainActivity.class)); // vào app chính
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
