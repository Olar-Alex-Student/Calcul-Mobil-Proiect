/*package com.example.wellness_tracker_usv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class ExerciseActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseFirestore db;
    private Spinner exerciseSpinner;
    private EditText durationInput;
    private TextView caloriesBurnedText,selectedTimeText,totalCaloriesText;
    private Button calculateButton, backButton,setNotificationButton,calculateTotalButton;


    private Map<String, Integer> exerciseCaloriesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Verifică și solicită permisiunea POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        exerciseSpinner = findViewById(R.id.exerciseSpinner);
        durationInput = findViewById(R.id.durationInput);
        caloriesBurnedText = findViewById(R.id.caloriesBurnedText);
        calculateButton = findViewById(R.id.calculateButton);
        backButton = findViewById(R.id.backButton);
        setNotificationButton = findViewById(R.id.setNotificationButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);
        totalCaloriesText = findViewById(R.id.totalCaloriesText);
        calculateTotalButton = findViewById(R.id.calculateTotalButton);

        // Load exercise data from Firebase
        loadExerciseData();

        // Preia userId din intenție
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Restul codului pentru activitate
        calculateTotalButton.setOnClickListener(v -> {
            String caloriesBurnedStr = caloriesBurnedText.getText().toString().replaceAll("[^\\d]", "");
            int caloriesBurned = caloriesBurnedStr.isEmpty() ? 0 : Integer.parseInt(caloriesBurnedStr);

            db.collection("users")
                    .whereEqualTo("userId", userId) // Folosește userId
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Double totalCalories = document.getDouble("totalCalories");
                                if (totalCalories == null) totalCalories = 0.0;

                                double remainingCalories = totalCalories - caloriesBurned;
                                totalCaloriesText.setText(String.format("Total Calories: %.2f", remainingCalories));
                            }
                        } else {
                            Toast.makeText(this, "No user data found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch total calories", Toast.LENGTH_SHORT).show();
                        Log.e("FetchError", "Error fetching total calories", e);
                    });
        });

        calculateTotalButton.setOnClickListener(v -> {
            // Obține caloriile calculate în primul textview
            String caloriesBurnedStr = caloriesBurnedText.getText().toString().replaceAll("[^\\d]", "");
            int caloriesBurned = caloriesBurnedStr.isEmpty() ? 0 : Integer.parseInt(caloriesBurnedStr);

            // Obține totalul caloriilor din baza de date
            db.collection("users")
                    .whereEqualTo("userId", "yourUserId") // Înlocuiește cu userId-ul real
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Double totalCalories = document.getDouble("totalCalories");
                                if (totalCalories == null) totalCalories = 0.0;

                                // Scade caloriile calculate
                                double remainingCalories = totalCalories - caloriesBurned;
                                totalCaloriesText.setText(String.format("Total Calories: %.2f", remainingCalories));
                            }
                        } else {
                            Toast.makeText(this, "No user data found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch total calories", Toast.LENGTH_SHORT).show();
                        Log.e("FetchError", "Error fetching total calories", e);
                    });
        });

        setNotificationButton.setOnClickListener(v -> {
            // Deschide un TimePicker pentru a selecta ora
            new android.app.TimePickerDialog(
                    ExerciseActivity.this,
                    (view, hourOfDay, minute) -> {
                        // Afișează ora selectată în TextView
                        selectedTimeText.setText(String.format("Selected time: %02d:%02d", hourOfDay, minute));

                        // Programează notificarea
                        scheduleNotification(hourOfDay, minute);
                    },
                    12, 0, true // Ora implicită: 12:00
            ).show();
        });


        // Calculate button action
        calculateButton.setOnClickListener(v -> {
            String durationStr = durationInput.getText().toString().trim();
            if (durationStr.isEmpty()) {
                Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration = Integer.parseInt(durationStr);
            String selectedExercise = (String) exerciseSpinner.getSelectedItem();
            Integer caloriesPerMinute = exerciseCaloriesMap.get(selectedExercise);

            if (caloriesPerMinute != null) {
                int totalCaloriesBurned = caloriesPerMinute * duration;
                caloriesBurnedText.setText(String.format("Calories burned: %d", totalCaloriesBurned));
            } else {
                Toast.makeText(this, "Error calculating calories", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button action
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Notifications will not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void loadExerciseData() {
        db.collection("exercises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> exerciseList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("Nume");
                        Long caloriesPerMinute = document.getLong("caloriesPerMinute");

                        if (name != null && caloriesPerMinute != null) {
                            exerciseList.add(name);
                            exerciseCaloriesMap.put(name, caloriesPerMinute.intValue());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    exerciseSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load exercises", Toast.LENGTH_SHORT).show();
                    Log.e("LoadError", "Error loading exercises", e);
                });
    }
    private void scheduleNotification(int hour, int minute) {
        // Creează un intent pentru NotificationReceiver
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Configurează timpul pentru notificare
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);

        // Setează notificarea folosind AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Notification set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
        }
    }
}
*/
package com.example.wellness_tracker_usv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExerciseActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseFirestore db;
    private Spinner exerciseSpinner;
    private EditText durationInput;
    private TextView caloriesBurnedText, selectedTimeText, totalCaloriesText;
    private Button calculateButton, backButton, setNotificationButton, calculateTotalButton;

    private Map<String, Integer> exerciseCaloriesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Verifică și solicită permisiunea POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        exerciseSpinner = findViewById(R.id.exerciseSpinner);
        durationInput = findViewById(R.id.durationInput);
        caloriesBurnedText = findViewById(R.id.caloriesBurnedText);
        calculateButton = findViewById(R.id.calculateButton);
        backButton = findViewById(R.id.backButton);
        setNotificationButton = findViewById(R.id.setNotificationButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);
        totalCaloriesText = findViewById(R.id.totalCaloriesText);
        calculateTotalButton = findViewById(R.id.calculateTotalButton);

        // Preia userId din intenție
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load exercise data from Firebase
        loadExerciseData();

        setNotificationButton.setOnClickListener(v -> {
            // Deschide un TimePicker pentru a selecta ora
            new android.app.TimePickerDialog(
                    ExerciseActivity.this,
                    (view, hourOfDay, minute) -> {
                        // Afișează ora selectată în TextView
                        selectedTimeText.setText(String.format("Selected time: %02d:%02d", hourOfDay, minute));

                        // Programează notificarea
                        scheduleNotification(hourOfDay, minute);
                    },
                    12, 0, true // Ora implicită: 12:00
            ).show();
        });


        // Calculate button action
        /*
        calculateButton.setOnClickListener(v -> {
            String durationStr = durationInput.getText().toString().trim();
            if (durationStr.isEmpty()) {
                Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration = Integer.parseInt(durationStr);
            String selectedExercise = (String) exerciseSpinner.getSelectedItem();
            Integer caloriesPerMinute = exerciseCaloriesMap.get(selectedExercise);

            if (caloriesPerMinute != null) {
                int totalCaloriesBurned = caloriesPerMinute * duration;
                caloriesBurnedText.setText(String.format("Calories burned: %d", totalCaloriesBurned));

                // Scade caloriile calculate din totalCalories din baza de date
                db.collection("users")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();
                                Double totalCalories = document.getDouble("totalCalories");
                                if (totalCalories == null) totalCalories = 0.0;

                                double updatedCalories = totalCalories - totalCaloriesBurned;

                                // Actualizează totalCalories în baza de date
                                db.collection("users").document(documentId)
                                        .update("totalCalories", updatedCalories)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Calories burned and total updated!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update total calories", Toast.LENGTH_SHORT).show();
                                            Log.e("UpdateError", "Error updating total calories", e);
                                        });

                                // Afișează totalul actualizat în UI
                                totalCaloriesText.setText(String.format("Total Calories: %.2f", updatedCalories));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            Log.e("FetchError", "Error fetching user data", e);
                        });
            } else {
                Toast.makeText(this, "Error calculating calories", Toast.LENGTH_SHORT).show();
            }
        });
        */
        calculateButton.setOnClickListener(v -> {
            String durationStr = durationInput.getText().toString().trim();

            if (durationStr.isEmpty()) {
                Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show();
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number for duration", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedExercise = (String) exerciseSpinner.getSelectedItem();
            Integer caloriesPerMinute = exerciseCaloriesMap.get(selectedExercise);

            if (caloriesPerMinute != null) {
                int totalCaloriesBurned = caloriesPerMinute * duration;
                caloriesBurnedText.setText(String.format("Calories burned: %d", totalCaloriesBurned));

                // Scade caloriile calculate din totalCalories din baza de date
                db.collection("users")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String documentId = document.getId();
                                Double totalCalories = document.getDouble("totalCalories");
                                if (totalCalories == null) totalCalories = 0.0;

                                double updatedCalories = totalCalories - totalCaloriesBurned;

                                // Actualizează totalCalories în baza de date
                                db.collection("users").document(documentId)
                                        .update("totalCalories", updatedCalories)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Calories burned and total updated!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update total calories", Toast.LENGTH_SHORT).show();
                                            Log.e("UpdateError", "Error updating total calories", e);
                                        });

                                // Afișează totalul actualizat în UI
                                totalCaloriesText.setText(String.format("Total Calories: %.2f", updatedCalories));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            Log.e("FetchError", "Error fetching user data", e);
                        });
            } else {
                Toast.makeText(this, "Error calculating calories", Toast.LENGTH_SHORT).show();
            }
        });


        // Total calories button action
        calculateTotalButton.setOnClickListener(v -> {
            db.collection("users")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Double totalCalories = document.getDouble("totalCalories");
                            if (totalCalories == null) totalCalories = 0.0;

                            // Afișează totalul caloriilor din baza de date
                            totalCaloriesText.setText(String.format("Total Calories: %.2f", totalCalories));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch total calories", Toast.LENGTH_SHORT).show();
                        Log.e("FetchError", "Error fetching total calories", e);
                    });
        });

        // Back button action
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Notifications will not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadExerciseData() {
        db.collection("exercises")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> exerciseList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("Nume");
                        Long caloriesPerMinute = document.getLong("caloriesPerMinute");

                        if (name != null && caloriesPerMinute != null) {
                            exerciseList.add(name);
                            exerciseCaloriesMap.put(name, caloriesPerMinute.intValue());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    exerciseSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load exercises", Toast.LENGTH_SHORT).show();
                    Log.e("LoadError", "Error loading exercises", e);
                });
    }

    private void scheduleNotification(int hour, int minute) {
        // Creează un intent pentru NotificationReceiver
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Configurează timpul pentru notificare
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);

        // Setează notificarea folosind AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Notification set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
        }
    }
}
