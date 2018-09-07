package com.mycollegepass.mycollegepass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class OpenActivity extends AppCompatActivity {

    private Button mLogIn;
    private Button mSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        mLogIn = findViewById(R.id.signin_open);
        mSignUp = findViewById(R.id.signup_open);

        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start LoginActivity
                Intent intent = new Intent(OpenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start LoginActivity
                Intent intent = new Intent(OpenActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
