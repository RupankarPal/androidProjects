package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.google.firebase.auth.FirebaseAuth;

public class loginPage extends AppCompatActivity {

    Button signup_btn;
    Button login_btn;
    EditText email_edt;
    EditText password_edt;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        email_edt = findViewById(R.id.username);
        password_edt = findViewById(R.id.password);

        signup_btn = findViewById(R.id.signup_btn);
        login_btn =findViewById(R.id.login_btn);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(loginPage.this, SignUp_PageActivity.class);
                startActivity(intent);

            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_edt.getText().toString().trim();
                String pass = password_edt.getText().toString().trim();
                
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(loginPage.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    login_btn.setEnabled(false);
                    android.util.Log.i("AuthDebug", "Login clicked. Email: " + email);
                    
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            android.util.Log.i("AuthDebug", "Firebase Login Success for: " + email + ". Generating OTP...");
                            // Step 2: Generate and send DETERMINISTIC OTP
                            String otp = otp_generater.otpgenerater(loginPage.this);
                            EmailHelper emailHelper = new EmailHelper();
                            emailHelper.sendEmail(email, "OTP Verification For DTrade", "Your OTP is: " + otp, new EmailHelper.EmailCallback() {
                                @Override
                                public void onSuccess() {
                                    android.util.Log.i("AuthDebug", "OTP Generated & Logged. Opening verification window.");
                                    Intent intent = new Intent(loginPage.this, OtpVerificationActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("mode", "LOGIN");
                                    startActivity(intent);
                                    login_btn.setEnabled(true);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    android.util.Log.e("AuthDebug", "OTP Generation Failed: " + e.getMessage());
                                    login_btn.setEnabled(true);
                                    Toast.makeText(loginPage.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown Firebase Error";
                            android.util.Log.e("AuthDebug", "Login Failed (Firebase): " + error);
                            login_btn.setEnabled(true);
                            Toast.makeText(loginPage.this, "Login failed (Firebase): " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }
}