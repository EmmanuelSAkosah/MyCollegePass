package com.mycollegepass.mycollegepass;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPassword extends AppCompatActivity {

    private String TAG = "LoginActivity";
    private Button submit;
    private ImageButton donutworry;
    private TextView email_TV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        /***get reference to UI elements***/
        submit = findViewById(R.id.submit_FP);
        donutworry = findViewById(R.id.donut_worry);
        email_TV = findViewById(R.id.email_FP);


        /***assign click listeners***/
        donutworry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ForgotPassword.this, "donut worry :) ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email_TV.getText().toString();
                resetUserPassword(email);

            }
            });
    }

    private void resetUserPassword( String email){
       /* FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = "SOME-SECURE-PASSWORD";

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Your password updated.");
                        }
                    }
                }); */

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this,"A link to reset your password have been sent your email",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

}
