package com.example.trexpense_2f94d;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddFragment extends Fragment {

    private Button currencyButton;
    private EditText balanceEditText;
    private TextView selectCategoryTextView, noteTextView, dateTextView, walletTextView;
    private List<Wallet> wallets;
    private String selectedWalletId;
    private String selectedWalletName;
    private Double selectedWalletBalance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final String DEFAULT_WALLET_NAME = "Default Wallet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        currencyButton = view.findViewById(R.id.currencyButton);
        balanceEditText = view.findViewById(R.id.balanceEditText);
        selectCategoryTextView = view.findViewById(R.id.selectCategoryTextView);
        noteTextView = view.findViewById(R.id.noteTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        walletTextView = view.findViewById(R.id.walletTextView);
        Button addButton = view.findViewById(R.id.addButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        wallets = new ArrayList<>();

        // Fetch current wallet from Firestore
        fetchCurrentWallet();

        // Set click listeners
        currencyButton.setOnClickListener(v -> {
            // Handle currency button click
        });

        selectCategoryTextView.setOnClickListener(v -> openCategoryDialog());
        noteTextView.setOnClickListener(v -> openNoteDialog());
        dateTextView.setOnClickListener(v -> openDatePickerDialog());
        walletTextView.setOnClickListener(v -> openWalletDialog());

        addButton.setOnClickListener(v -> uploadTransaction());

        balanceEditText.requestFocus();

        return view;
    }

    private void fetchCurrentWallet() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get() // Get the user document
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String currentWalletId = document.getString("currentWallet");

                            if (currentWalletId != null) {
                                // Fetch wallet details using currentWalletId
                                fetchWalletDetails(currentWalletId);
                            } else {
                                Toast.makeText(getContext(), "No current wallet found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchWalletDetails(String walletId) {
        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("wallets")
                .document(walletId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        selectedWalletId = documentSnapshot.getId();
                        selectedWalletName = documentSnapshot.getString("name");
                        selectedWalletBalance = documentSnapshot.getDouble("balance");

                        // Update UI with wallet details
                        walletTextView.setText(selectedWalletName);
                    } else {
                        Toast.makeText(getContext(), "Wallet not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch wallet data", Toast.LENGTH_SHORT).show());
    }

    private void openCategoryDialog() {
        String[] categories = getResources().getStringArray(R.array.category_array);
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Category")
                .setItems(categories, (dialog, which) -> selectCategoryTextView.setText(categories[which]))
                .show();
    }

    private void openNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Note");

        EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton("Done", (dialog, which) -> noteTextView.setText(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(dateTextView.getContext(), (view, year1, month1, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dateTextView.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void openWalletDialog() {
        // Fetch all available wallets for the user
        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("wallets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> walletNames = new ArrayList<>();
                    List<String> walletIds = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        walletNames.add(document.getString("name"));
                        walletIds.add(document.getString("id"));
                    }

                    new AlertDialog.Builder(getActivity())
                            .setTitle("Select Wallet")
                            .setItems(walletNames.toArray(new String[0]), (dialog, which) -> {
                                String selectedWalletName = walletNames.get(which);
                                String selectedWalletId = walletIds.get(which);
                                // Update currentWallet in the user document with the selected wallet ID
                                updateCurrentWallet(selectedWalletName, selectedWalletId);
                            })
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching wallets", Toast.LENGTH_SHORT).show());
    }

    private void updateCurrentWallet(String walletName, String walletId) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("currentWallet", walletId) // Update current wallet reference
                .addOnSuccessListener(aVoid -> {
                    walletTextView.setText(walletName); // Update wallet name in the UI
//                    Toast.makeText(getContext(), "Wallet updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating wallet", Toast.LENGTH_SHORT).show());
    }

    private void uploadTransaction() {
        String transactionAmountStr = balanceEditText.getText().toString();
        String category = selectCategoryTextView.getText().toString();
        String note = noteTextView.getText().toString();
        String date = dateTextView.getText().toString();
        String dateToUpload;

        if (date.equals("Today")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateToUpload = dateFormat.format(new Date());
        } else {
            dateToUpload = date;
        }

        if (transactionAmountStr.isEmpty() || category.equals("Select Category")) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedWalletId == null) {
            Toast.makeText(getContext(), "No wallet selected", Toast.LENGTH_SHORT).show();
            return;
        }

        double transactionAmount = Double.parseDouble(transactionAmountStr);

        if (transactionAmount > selectedWalletBalance) {
            Toast.makeText(getContext(), "Insufficient balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("amount", transactionAmount);
        transactionData.put("category", category);
        transactionData.put("note", note);
        transactionData.put("date", dateToUpload);

        String transactionId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("wallets").document(selectedWalletId)
                .collection("transactions")
                .document(transactionId)
                .set(transactionData)
                .addOnSuccessListener(aVoid -> {
                    // Update wallet balance
                    selectedWalletBalance -= transactionAmount;

                    Map<String, Object> updatedWalletData = new HashMap<>();
                    updatedWalletData.put("balance", selectedWalletBalance);

                    db.collection("users").document(userId)
                            .collection("wallets").document(selectedWalletId)
                            .update(updatedWalletData);

                    Toast.makeText(getContext(), "Transaction uploaded", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error uploading transaction", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        balanceEditText.setText("");
        selectCategoryTextView.setText("Select Category");
        noteTextView.setText("");
        dateTextView.setText("Today");
    }

    // Wallet class for storing wallet details
    public static class Wallet {
        private String id;
        private String name;
        private Double balance;

        public Wallet(String id, String name, Double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Double getBalance() {
            return balance;
        }
    }
}
