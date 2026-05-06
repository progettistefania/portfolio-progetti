package com.example.myappusato.model;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myappusato.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button signin;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.pass);
        signin = findViewById(R.id.sign_in_btn);
        signup = findViewById(R.id.sing_up_text);



        //va all'activity di sign_up

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Intent i1 = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i1);
            }
        });

        //Sign in
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Prendo l'istanza di Firebase e Email e Password inseriti dall'utente
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString();

                //Se i campi sono vuoti, impedisce di loggare
                if(email.isEmpty() || password.isEmpty())
                    Toast.makeText(LoginActivity.this, "Missing field", Toast.LENGTH_SHORT).show();
                else {
                    //Loggo
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(LoginActivity.this, BrowsingActivity.class);
                                        startActivity(i);
                                        emailField.getText().clear();
                                        passwordField.getText().clear();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Login Error", Toast.LENGTH_LONG).show();
                                        emailField.getText().clear();
                                        passwordField.getText().clear();
                                    }
                                }
                            });
                }
            }
        });



    }

}