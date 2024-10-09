package com.example.trexpense_2f94d; // Replace with your package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;



public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Spinner currencySpinner;
    private String selectedCurrency;
    private Button signupButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Ensure this matches your layout file name

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        currencySpinner = findViewById(R.id.currencySpinner);
        signupButton = findViewById(R.id.signupButton);

        // Setup currency spinner
        setupCurrencySpinner();

        // Set OnClickListener for the Sign Up button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void setupCurrencySpinner() {
        // Load currency array from resources
        String[] currencies = getResources().getStringArray(R.array.currency_list);

        // Create an ArrayAdapter using the currency array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencies);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        currencySpinner.setAdapter(adapter);

        // Set default selection to "Canadian Dollar (CAD)"
        currencySpinner.setSelection(5); // Index 5 corresponds to CAD

        // Set up a listener to get the selected currency
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = currencies[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // If nothing is selected, default to CAD
                selectedCurrency = currencies[5]; // CAD
                currencySpinner.setSelection(5); // Set the spinner to CAD
            }
        });
    }

    private void signUp() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String currency = selectedCurrency;

        // Validate inputs
        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful.", Toast.LENGTH_SHORT).show();

                        String userId = user.getUid();

                        // Create a user profile in Firestore
                        UserProfile userProfile = new UserProfile(firstName, lastName, email, currency, userId);
                        db.collection("users").document(userId)
                                .set(userProfile)
                                .addOnSuccessListener(aVoid -> {
                                    // Document was successfully written
                                    Intent intent = new Intent(SignUpActivity.this, CreateWalletActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this, "Error saving user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public class UserProfile {
        private String firstName;
        private String lastName;
        private String email;
        private String currency;
        private String userId;

        // Constructor
        public UserProfile(String firstName, String lastName, String email, String currency, String userId) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.currency = currency;
            this.userId = userId;
        }

        // Getter for firstName
        public String getFirstName() {
            return firstName;
        }

        public String getUserId() {
            return userId;
        }

        // Setter for firstName
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        // Getter for lastName
        public String getLastName() {
            return lastName;
        }

        // Setter for lastName
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        // Getter for email
        public String getEmail() {
            return email;
        }

        // Setter for email
        public void setEmail(String email) {
            this.email = email;
        }

        // Getter for currency
        public String getCurrency() {
            return currency;
        }

        // Setter for currency
        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

}
