package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.MainActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText otpInput;
    private TextView otpTimerTxt;
    private String userEmail;
    private String userPassword;
    private String mode;
    private Context context;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();

        userEmail = getIntent().getStringExtra("email");
        userPassword = getIntent().getStringExtra("password");
        mode = getIntent().getStringExtra("mode");

        otpInput = findViewById(R.id.otp_input);
        otpTimerTxt = findViewById(R.id.otp_timer_txt);
        Button verifyBtn = findViewById(R.id.verify_otp_btn);
        TextView resendTxt = findViewById(R.id.resend_otp_txt);
        TextView instructionTxt = findViewById(R.id.otp_instruction);

        if (userEmail != null) {
            instructionTxt.setText("Enter the 6-digit code sent to " + userEmail);
        }

        startOtpTimer();

        verifyBtn.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString().trim();
            
            if (otp_generater.verifyOtp(this, enteredOtp)) {
                if ("SIGNUP".equals(mode)) {
                    // Create account after OTP verified
                    mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            Toast.makeText(this, "Signup Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Verification Successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
            } else {
                Toast.makeText(this, "Invalid OTP. Code may have expired.", Toast.LENGTH_LONG).show();
            }
        });

        resendTxt.setOnClickListener(v -> {
            String otp = otp_generater.otpgenerater(this);
            EmailHelper emailHelper= new EmailHelper();
            emailHelper.sendEmail(userEmail, "OTP Verification For DTrade", "Your OTP is: " + otp, new EmailHelper.EmailCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(OtpVerificationActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    startOtpTimer(); // Restart timer on resend
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OtpVerificationActivity.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void startOtpTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(120000, 1000) { // 120 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                otpTimerTxt.setText("Expires in: " + timeFormatted);
            }

            @Override
            public void onFinish() {
                otpTimerTxt.setText("Code Expired");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
