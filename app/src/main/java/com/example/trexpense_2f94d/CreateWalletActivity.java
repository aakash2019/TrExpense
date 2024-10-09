package com.example.trexpense_2f94d;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.UUID;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateWalletActivity extends AppCompatActivity {
    private EditText walletNameInput, balanceInput;
    private Spinner currencySpinner;
    private String selectedCurrency;
    private Button createWalletButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        walletNameInput = findViewById(R.id.walletNameInput);
        balanceInput = findViewById(R.id.balanceInput);
        currencySpinner = findViewById(R.id.currencySpinner);
        createWalletButton = findViewById(R.id.createWalletButton);

        // Set up the currency spinner
        setupCurrencySpinner();

        //Create a wallet
        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createWallet();
            }
        });
    }

    private void createWallet() {
        String walletName = walletNameInput.getText().toString().trim();
        String balanceString = balanceInput.getText().toString().trim();
        double balance = balanceString.isEmpty() ? 0.0 : Double.parseDouble(balanceString);

        // Check if wallet name is empty
        if (walletName.isEmpty()) {
            Toast.makeText(this, "Please enter a wallet name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new Wallet object
        String walletId = UUID.randomUUID().toString();
        Wallet wallet = new Wallet(walletId, walletName, selectedCurrency, balance);

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Save the wallet under the user's collection
            db.collection("users").document(userId).collection("wallets").document(walletId)
                    .set(wallet)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(CreateWalletActivity.this, "Wallet created successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateWalletActivity.this, TabsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateWalletActivity.this, "Error creating wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
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

    public class Wallet {
        private String id;
        private String name;
        private String currency;
        private double balance;

        // Constructor
        public Wallet(String id, String name, String currency, double balance) {
            this.id = id;
            this.name = name;
            this.currency = currency;
            this.balance = balance;
        }

        // Getters and Setters for each field
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

}