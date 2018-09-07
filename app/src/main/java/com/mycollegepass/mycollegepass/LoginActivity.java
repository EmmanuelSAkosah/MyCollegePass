package com.mycollegepass.mycollegepass;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mycollegepass.mycollegepass.model.Preferences;
import com.mycollegepass.mycollegepass.utils.InternetManager;


public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";
    private TextView createAccount;
    private TextView forgot_password;
    private ImageButton donut;
    private TextView email_TV;
    private TextView password_TV;
    private FirebaseAuth mAuth;
    private Preferences mPrefs;

    /******/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = new Preferences(this);

        /***get reference to a fireBase instant***/
        mAuth = FirebaseAuth.getInstance();

        /***get reference to UI elements***/
        createAccount = findViewById(R.id.create_account);
        donut = findViewById(R.id.donut_LI);
        email_TV = findViewById(R.id.email_LI);
        password_TV = findViewById(R.id.password_LI);
        forgot_password= findViewById(R.id.forgot_password);

        /***assign click listeners***/
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start LoginActivity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start ForgotPassActivity
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        donut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = email_TV.getText().toString();
                String password = password_TV.getText().toString();

                //sign in via fireBase
                if (email.isEmpty() || password.isEmpty()) {
                    //showProgress(true);
                    Toast.makeText(LoginActivity.this, R.string.empty_email_password_warning,
                            Toast.LENGTH_SHORT).show();
                } else {
                    //showProgress(true);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //showProgress(false);
                                    if (task.isSuccessful()) {
                                        mPrefs.setUserLoggedInStatus(true);
                                        Log.d(TAG, "sign in status set: "
                                                + mPrefs.getUserLoggedInStatus());

                                        //open MainActivity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        if(!InternetManager.isNetworkAvailable(getApplication()))
                                        {
                                            Toast.makeText(LoginActivity.this, "Login unsuccessful.\nNo internet Connection",
                                                    Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(LoginActivity.this, R.string.unregistered_text,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }

            }
        });
    }


}
