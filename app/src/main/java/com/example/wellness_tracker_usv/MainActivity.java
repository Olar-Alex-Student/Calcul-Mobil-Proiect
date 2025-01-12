
package com.example.wellness_tracker_usv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inițializează Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Legături către input-uri și buton
        EditText inputEmail = findViewById(R.id.inputEmail);
        EditText inputPassword = findViewById(R.id.inputPassword);
        Button loginButton = findViewById(R.id.loginButton);

        /*
        loginButton.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificare date în Firebase Firestore
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean userFound = false;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String storedPassword = document.getString("password");
                            String userId = document.getString("userId");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                userFound = true;

                                if (userId != null && !userId.isEmpty()) {
                                    // Navighează la WelcomeActivity cu userId
                                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                    intent.putExtra("USER_ID", userId);
                                    startActivity(intent);
                                    finish(); // Oprește activitatea curentă
                                } else {
                                    Toast.makeText(this, "User ID not found. Contact support.", Toast.LENGTH_SHORT).show();
                                    Log.e("LoginError", "User ID is missing in Firestore document.");
                                }
                                break;
                            }
                        }

                        if (!userFound) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginError", "Error fetching data", e);
                        Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                    });
        });
        */
        loginButton.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // Verifică dacă câmpurile sunt goale
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifică dacă email-ul conține '@'
            if (!email.contains("@")) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificare date în Firebase Firestore
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean userFound = false;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String storedPassword = document.getString("password");
                            String userId = document.getString("userId");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                userFound = true;

                                if (userId != null && !userId.isEmpty()) {
                                    // Navighează la WelcomeActivity cu userId
                                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                                    intent.putExtra("USER_ID", userId);
                                    startActivity(intent);
                                    finish(); // Oprește activitatea curentă
                                } else {
                                    Toast.makeText(this, "User ID not found. Contact support.", Toast.LENGTH_SHORT).show();
                                    Log.e("LoginError", "User ID is missing in Firestore document.");
                                }
                                break;
                            }
                        }

                        if (!userFound) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginError", "Error fetching data", e);
                        Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                    });
        });

    }
}
