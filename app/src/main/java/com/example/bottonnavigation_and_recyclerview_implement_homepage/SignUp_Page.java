package com.example.bottonnavigation_and_recyclerview_implement_homepage;

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

public class SignUp_Page extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);
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
                Intent intent = new Intent(SignUp_Page.this, loginPage.class);
                startActivity(intent);
            }
        });

        EditText username,password,reenter_password;
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        reenter_password = findViewById(R.id.reenter_password);

        Button signup_btn;
        signup_btn = findViewById(R.id.signup_btn);

        String notFill_alart = "Please fill in all the fields";
        String wrongPassword_alart = "Password and re-enter password are not match";
        String conformation = "Your account has been created successfully";

        if(!username.getText().toString().equals("") &&
           !password.getText().toString().equals("") &&
           !reenter_password.getText().toString().equals("") &&
           password.getText().toString().equals(reenter_password.getText().toString())){

            signup_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SignUp_Page.this, loginPage.class);

                    Toast.makeText(SignUp_Page.this,conformation,Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            });

        }else if (!password.getText().toString().equals(reenter_password.getText().toString())){
            Toast.makeText(SignUp_Page.this,wrongPassword_alart,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(SignUp_Page.this,notFill_alart,Toast.LENGTH_SHORT).show();
        }

    }
}