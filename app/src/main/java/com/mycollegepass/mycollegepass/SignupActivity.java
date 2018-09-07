package com.mycollegepass.mycollegepass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.mycollegepass.mycollegepass.utils.DatabaseOperations;
import com.mycollegepass.mycollegepass.utils.InternetManager;

public class SignupActivity extends AppCompatActivity {

    private String TAG = "SignUpActivity";
    private TextView haveAccount;
    private TextView email_TV;
    private TextView password_TV;
    private TextView first_name;
    private TextView last_name;
    private ImageButton donut;
    private FirebaseAuth mAuth;
    private DatabaseOperations DBOps = new DatabaseOperations();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /***get reference to a fireBase instant***/
        mAuth = FirebaseAuth.getInstance();

        haveAccount = findViewById(R.id.have_account);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email_TV = findViewById(R.id.email_SU);
        password_TV = findViewById(R.id.password_SU);
        donut = findViewById(R.id.donut_SU);


        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start LoginActivity
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        donut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = email_TV.getText().toString();
                String password = password_TV.getText().toString();

                if(!inputIsValid(email,password)){
                } else {
                    Log.d(TAG, "Email: "+email+"password: "+password);
                    mAuth.createUserWithEmailAndPassword(email, password) //check for empty string
                            .addOnCompleteListener(SignupActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign up success, u
                                                Log.d(TAG, "createUserWithEmail:success");
                                                Toast.makeText(SignupActivity.this, "Sign up successful.",
                                                        Toast.LENGTH_LONG).show();
                                                //TODO: wait a bit to show toast before opening main activity

                                                //initiate verification
                                                verifyEmail();

                                                DBOps.StoreUserIDInDB();
                                                //open login page
                                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                startActivity(intent);

                                            } else {

                                                // If sign in fails, display a message to the user.
                                                if(!InternetManager.isNetworkAvailable(getApplication()))
                                                {
                                                    Toast.makeText(SignupActivity.this, "No internet Connection",
                                                            Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(SignupActivity.this, "Sign up failed:"
                                                                    + task.getException().getMessage(),
                                                            Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        }
                                    });

                }
            }
        });

    }

    public void verifyEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.reload();  //refresh status

        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this,
                                    "Verification email sent to" + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            //update Database user as verified

                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            if(!InternetManager.isNetworkAvailable(getApplication()))
                            {
                                Toast.makeText(SignupActivity.this, "No internet Connection",
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(SignupActivity.this,
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private boolean inputIsValid(String email, String password){
       if (email.isEmpty() || password.isEmpty()) {
           //showProgress(true);
           Toast.makeText(SignupActivity.this, R.string.empty_email_password_warning,
                   Toast.LENGTH_SHORT).show();
           return false;
       }

       //check for edu ending
        int length = email.trim().length();
        int startIndex = length-3; //3 is length of edu
        String last3 = email.substring(startIndex);
        if( !last3.equals("edu")){
            Toast.makeText(SignupActivity.this, "Must have a college email :(",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
