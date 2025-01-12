
package com.example.wellness_tracker_usv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner foodSpinner;
    private TextView gramInput;
    private TextView totalCaloriesText;
    private Button calculateButton;
    private Button saveButton;
    private Button totalCaloriesButton;
    private TextView resultText;

    private Button exerciseButton,resetButton;
    private Map<String, Integer> foodCaloriesMap = new HashMap<>();
    private String selectedFood;
    private double calculatedCalories;
    private double totalCalories = 0;

    private String userId; // userId obținut din Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Obține userId din Intent
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        foodSpinner = findViewById(R.id.foodSpinner);
        gramInput = findViewById(R.id.gramInput);
        totalCaloriesText = findViewById(R.id.totalCaloriesText);
        calculateButton = findViewById(R.id.calculateButton);
        saveButton = findViewById(R.id.saveButton);
        totalCaloriesButton = findViewById(R.id.totalCaloriesButton);
        resultText = findViewById(R.id.resultText);
        exerciseButton = findViewById(R.id.exerciseButton);
        resetButton = findViewById(R.id.resetButton);


        // Load food data from Firebase
        loadFoodData();


        // Funcționalitate pentru butonul Reset
        resetButton.setOnClickListener(v -> {
            db.collection("users")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();

                                // Resetează totalCalories la 0
                                db.collection("users").document(documentId)
                                        .update("totalCalories", 0.0)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Total calories reset to 0!", Toast.LENGTH_SHORT).show();
                                            totalCaloriesText.setText("Total Calories: 0.00");
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to reset total calories", Toast.LENGTH_SHORT).show();
                                            Log.e("ResetError", "Error resetting total calories", e);
                                        });
                            }
                        } else {
                            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                        Log.e("FetchError", "Error fetching user data", e);
                    });
        });

        exerciseButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ExerciseActivity.class);
            intent.putExtra("USER_ID", userId); // Transmite userId către ExerciseActivity
            startActivity(intent);
        });


        // Calculate button action
        /*
        calculateButton.setOnClickListener(v -> {
            String gramsStr = gramInput.getText().toString();
            if (gramsStr.isEmpty()) {
                Toast.makeText(this, "Please enter the weight in grams", Toast.LENGTH_SHORT).show();
                return;
            }

            int grams = Integer.parseInt(gramsStr);
            selectedFood = (String) foodSpinner.getSelectedItem();
            Integer caloriesPer100g = foodCaloriesMap.get(selectedFood);

            if (caloriesPer100g != null) {
                calculatedCalories = (caloriesPer100g * grams) / 100.0;
                totalCalories += calculatedCalories;
                totalCaloriesText.setText(String.format("Total Calories: %.2f", totalCalories));
                resultText.setText(String.format("%s (%.0fg) contains %.2f calories", selectedFood, (float) grams, calculatedCalories));
            } else {
                Toast.makeText(this, "Error calculating calories", Toast.LENGTH_SHORT).show();
            }
        });
         */
        calculateButton.setOnClickListener(v -> {
            String gramsStr = gramInput.getText().toString().trim();

            if (gramsStr.isEmpty()) {
                Toast.makeText(this, "Please enter the weight in grams", Toast.LENGTH_SHORT).show();
                return;
            }

            int grams;
            try {
                grams = Integer.parseInt(gramsStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for weight in grams", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedFood = (String) foodSpinner.getSelectedItem();
            Integer caloriesPer100g = foodCaloriesMap.get(selectedFood);

            if (caloriesPer100g != null) {
                calculatedCalories = (caloriesPer100g * grams) / 100.0;
                totalCalories += calculatedCalories;
                totalCaloriesText.setText(String.format("Total Calories: %.2f", totalCalories));
                resultText.setText(String.format("%s (%.0fg) contains %.2f calories", selectedFood, (float) grams, calculatedCalories));
            } else {
                Toast.makeText(this, "Error calculating calories", Toast.LENGTH_SHORT).show();
            }
        });

        // Save button action
        saveButton.setOnClickListener(v -> {
            if (calculatedCalories == 0) {
                Toast.makeText(this, "Please calculate the calories first", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> entryData = new HashMap<>();
            entryData.put("food", selectedFood);
            entryData.put("calories", calculatedCalories);

            // Salvează intrarea în user_calories
            db.collection("user_calories").add(entryData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();

                        // Caută documentul utilizatorului pe baza câmpului userId
                        db.collection("users")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            String documentId = document.getId(); // Obține ID-ul documentului
                                            Double currentTotal = document.getDouble("totalCalories");
                                            if (currentTotal == null) currentTotal = 0.0;

                                            double newTotal = currentTotal + calculatedCalories;

                                            // Actualizează câmpul totalCalories
                                            db.collection("users").document(documentId)
                                                    .update("totalCalories", newTotal)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Total calories updated!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Failed to update total calories", Toast.LENGTH_SHORT).show();
                                                        Log.e("UpdateError", "Error updating total calories", e);
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                    Log.e("FetchError", "Error fetching user data", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
                        Log.e("SaveError", "Error saving data", e);
                    });
        });

        // Total calories button action
        totalCaloriesButton.setOnClickListener(v -> {
            db.collection("users")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Double totalCalories = document.getDouble("totalCalories");
                            if (totalCalories == null) totalCalories = 0.0;

                            totalCaloriesText.setText(String.format("Total Calories: %.2f", totalCalories));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        Log.e("LoadError", "Error loading user data", e);
                    });
        });
    }

    private void loadFoodData() {
        db.collection("foods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> foodList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("Nume");
                        Long calories = document.getLong("Calorii");

                        if (name != null && calories != null) {
                            foodList.add(name);
                            foodCaloriesMap.put(name, calories.intValue());
                        }
                    }

                    // Sortează lista de alimente în ordine alfabetică
                    Collections.sort(foodList);

                    // Creează și setează adapterul
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foodList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    foodSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load food data", Toast.LENGTH_SHORT).show();
                    Log.e("LoadError", "Error loading food data", e);
                });
    }

}
