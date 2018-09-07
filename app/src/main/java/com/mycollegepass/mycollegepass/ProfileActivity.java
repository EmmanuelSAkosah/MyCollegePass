package com.mycollegepass.mycollegepass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mycollegepass.mycollegepass.model.OwlMessages;

public class ProfileActivity extends AppCompatActivity {

    private TextView resetPassword;
    private ImageView owl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        /***get reference to UI elements***/
        resetPassword = findViewById(R.id.password_P);
        owl = findViewById(R.id.owl_P);


        /***assign click listeners***/
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start ForgotPassActivity
                Intent intent = new Intent(ProfileActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        owl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String message = "";
                String message = OwlMessages.getRandomMessage();
                Toast.makeText(ProfileActivity.this,
                        message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
