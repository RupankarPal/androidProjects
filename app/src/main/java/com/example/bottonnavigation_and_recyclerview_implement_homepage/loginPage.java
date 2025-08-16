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

public class loginPage extends AppCompatActivity {

    Button signup_btn;
    Button login_btn;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        signup_btn = findViewById(R.id.signup_btn);
        login_btn =findViewById(R.id.login_btn);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(loginPage.this, SignUp_Page.class);
                startActivity(intent);

            }
        });

        String notFill_alart = "Please fill in all the fields for login";

        if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
            login_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(loginPage.this, MainActivity.class);
                    startActivity(intent);
                }
            });

        }else {
            Toast.makeText(loginPage.this,notFill_alart,Toast.LENGTH_SHORT).show();
        }

    }


}