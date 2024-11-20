package com.example.trexpense_2f94d;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditWallet extends AppCompatActivity {

    private EditText walletNameInput, balanceInput;
    private Spinner currencySpinner;
    private String selectedCurrency;
    private Button editWalletButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String walletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wallet);

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        walletNameInput = findViewById(R.id.walletNameInput);
        balanceInput = findViewById(R.id.balanceInput);
        currencySpinner = findViewById(R.id.currencySpinner);
        editWalletButton = findViewById(R.id.editWalletButton);

        // Set up the currency spinner
        setupCurrencySpinner();

        // Get wallet ID from intent extras
        walletId = getIntent().getStringExtra("walletId");

        if (walletId != null) {
            // Load wallet data for editing
            loadWalletData(walletId);
        }

        // Set up button click listener for saving the wallet
        editWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateWallet(walletId);
            }
        });
    }

    private void saveOrUpdateWallet(String walletId) {
        // Get wallet name and balance from input fields
        String walletName = walletNameInput.getText().toString().trim();
        String balanceString = balanceInput.getText().toString().trim();
        double balance = balanceString.isEmpty() ? 0.0 : Double.parseDouble(balanceString);

        // Validate wallet name
        if (walletName.isEmpty()) {
            Toast.makeText(this, "Please enter a wallet name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare wallet data as a map
        Map<String, Object> walletData = new HashMap<>();
        walletData.put("name", walletName);
        walletData.put("currency", selectedCurrency);
        walletData.put("balance", balance);

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // If walletId is null, create a new wallet
            if (walletId == null) {
                walletId = UUID.randomUUID().toString();  // Generate a new wallet ID
                walletData.put("id", walletId);

                // Save new wallet to Firestore
                db.collection("users").document(userId).collection("wallets").document(walletId)
                        .update(walletData)
                        .addOnSuccessListener(aVoid -> {
                            // Optionally set this wallet as the current wallet
                            Toast.makeText(EditWallet.this, "Wallet updated succesfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditWallet.this, "Error creating wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Update existing wallet
                db.collection("users").document(userId).collection("wallets").document(walletId)
                        .update(walletData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditWallet.this, "Wallet updated", Toast.LENGTH_SHORT).show();
                            navigateToTabsActivity();
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditWallet.this, "Error updating wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWalletData(String walletId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("wallets").document(walletId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve wallet data as a map
                            Map<String, Object> walletData = documentSnapshot.getData();
                            if (walletData != null) {
                                String walletName = (String) walletData.get("name");
                                String currency = (String) walletData.get("currency");
                                Double balance = (Double) walletData.get("balance");

                                // Populate the input fields with retrieved data
                                walletNameInput.setText(walletName);
                                balanceInput.setText(String.valueOf(balance));
                                setCurrencySpinner(currency);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditWallet.this, "Error loading wallet data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrencySpinner(String currency) {
        String[] currencies = getResources().getStringArray(R.array.currency_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        // Set selected currency in the spinner
        int selectedPosition = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currency)) {
                selectedPosition = i;
                break;
            }
        }

        if (selectedPosition != -1) {
            currencySpinner.setSelection(selectedPosition);
        } else {
            // Set default to CAD
            currencySpinner.setSelection(5); // Index 5 corresponds to CAD
        }
    }

    private void setupCurrencySpinner() {
        String[] currencies = getResources().getStringArray(R.array.currency_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    private void navigateToTabsActivity() {
        Intent intent = new Intent(EditWallet.this, TabsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
