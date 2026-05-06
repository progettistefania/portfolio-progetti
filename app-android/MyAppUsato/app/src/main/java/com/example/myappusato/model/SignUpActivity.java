package com.example.myappusato.model;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myappusato.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button button;
    EditText emailField;
    EditText passwordField;
    EditText fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailField = findViewById(R.id.sign_up_email);
        fullName = findViewById(R.id.sing_up_name_surname);
        passwordField = findViewById(R.id.sing_up_password);
        button = findViewById(R.id.sing_up_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prendo l'istanza di Firebase e Email e Password inseriti dall'utente
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                String email = emailField.getText().toString().trim();
                String fName = fullName.getText().toString();
                String password = passwordField.getText().toString();

                //Se i campi sono vuoti, impedisce di registrarsi
                if(email.isEmpty() || password.isEmpty() || fName.isEmpty())
                    Toast.makeText(SignUpActivity.this, "Missing fields", Toast.LENGTH_SHORT).show();
                else {
                    //Registro l'utente
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //se successo aggiunge full name all'utente
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fName).build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                            Log.d(TAG, "User profile updated.");
                                                    }
                                                });

                                        Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                        finish();
                                    }
                                    else
                                        Toast.makeText(getApplicationContext(), "Registration Error", Toast.LENGTH_LONG).show();
                                }
                            });

                }
            }

        });

    }


}