package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.content.Context;
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

public class SignUp_PageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);
        
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button login_btn;
        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp_PageActivity.this, loginPage.class);
                startActivity(intent);
                finish();
            }
        });

        EditText email_edt, password_edt, reenter_password_edt;
        email_edt = findViewById(R.id.username);
        password_edt = findViewById(R.id.password);
        reenter_password_edt = findViewById(R.id.reenter_password);

        Button signup_btn = findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String emailaddress=email_edt.getText().toString();
                String password=password_edt.getText().toString();
                String reenter_password=reenter_password_edt.getText().toString();

                if (emailaddress.isEmpty() || password.isEmpty() || reenter_password.isEmpty()) {
                    Toast.makeText(SignUp_PageActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(reenter_password)) {
                    Toast.makeText(SignUp_PageActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                else{
                    otp_generater otpGenerater = new otp_generater();
                    String otp = otpGenerater.otpgenerater(SignUp_PageActivity.this);
                    EmailHelper emailHelper = new EmailHelper();
                    emailHelper.sendEmail(emailaddress, "OTP Verification For DTrade", "Your OTP is: " + otp, new EmailHelper.EmailCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(SignUp_PageActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp_PageActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", emailaddress);
                            intent.putExtra("password", password);
                            intent.putExtra("mode", "SIGNUP");
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(SignUp_PageActivity.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
