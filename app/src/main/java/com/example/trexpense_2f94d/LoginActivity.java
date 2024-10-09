package com.example.trexpense_2f94d;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput; // Changed to match your XML ID
    private EditText passwordInput; // Changed to match your XML ID
    private Button loginButton;
    private TextView forgotPassword; // Changed to match your XML ID
    private TextView signUpText; // Changed to match your XML ID

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpText = findViewById(R.id.signUpText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle forgot password functionality here
                Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Sign Up activity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim(); // Changed to match your XML ID
        String password = passwordInput.getText().toString().trim(); // Changed to match your XML ID

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Authenticate user with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, redirect to home screen
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        Intent intent = new Intent(LoginActivity.this, TabsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkWallet(FirebaseUser user){
        String userId = user.getUid();
        db.collection("users").document(userId)
                .collection("wallets")
                .limit(1) // Limit to 1 result to check if the collection has any documents
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // The collection has documents (exists)
                            Intent intent = new Intent(LoginActivity.this, TabsActivity.class);
                            startActivity(intent);
                            finish();
                            Log.d("FirestoreCheck", "Collection 'transactions' exists with documents.");
                        } else {
                            // The collection is empty (no documents, might not exist)
                            Intent intent = new Intent(LoginActivity.this, CreateWalletActivity.class);
                            startActivity(intent);
                            finish();
                            Log.d("FirestoreCheck", "Collection 'transactions' does not exist or is empty.");
                        }
                    } else {
                        // Handle the error
                        Log.d("FirestoreCheck", "Error checking collection: ", task.getException());
                    }
                });
    }
}
