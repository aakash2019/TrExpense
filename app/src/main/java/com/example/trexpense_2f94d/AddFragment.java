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
    private TextView addTransactionHeading;
    private Button currencyButton;
    private EditText balanceEditText;
    private TextView selectCategoryTextView, noteTextView, dateTextView, walletTextView;
    private List<Wallet> wallets; // List to hold wallet objects
    private String selectedWalletId; // To store selected wallet ID
    private Double selectedWalletBalance;
    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth auth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        addTransactionHeading = view.findViewById(R.id.addTransactionHeading);
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

        // Fetch wallets from Firestore
        fetchWalletsFromFirebase();

        // Set click listeners
        currencyButton.setOnClickListener(v -> {
            // Handle currency button click
        });

        selectCategoryTextView.setOnClickListener(v -> openCategoryDialog());
        noteTextView.setOnClickListener(v -> openNoteDialog());
        dateTextView.setOnClickListener(v -> openDatePickerDialog());
        walletTextView.setOnClickListener(v -> openWalletDialog());

        addButton.setOnClickListener(v -> uploadTransaction());

        // Automatically focus on balance input when the fragment is opened
        balanceEditText.requestFocus();

        return view;
    }

    private void fetchWalletsFromFirebase() {
        String userId = auth.getCurrentUser().getUid(); // Get current user ID

        db.collection("users").document(userId)
                .collection("wallets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String walletId = document.getId();
                            String walletName = document.getString("name");
                            double walletBalance = document.getDouble("balance");
                            wallets.add(new Wallet(walletId, walletName, walletBalance));
                        }
                    }
                });
    }

    private void openCategoryDialog() {
        // Implement a dialog to choose category and set selected category
        String[] categories = getResources().getStringArray(R.array.category_array);
        new AlertDialog.Builder(getActivity())
                .setTitle("Select Category")
                .setItems(categories, (dialog, which) -> {
                    selectCategoryTextView.setText(categories[which]);
                })
                .show();
    }

    private void openNoteDialog() {
        // Implement a dialog to input note
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Note");

        EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton("Done", (dialog, which) -> {
            String note = input.getText().toString();
            noteTextView.setText(note);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

//      Dialog box to show date
        DatePickerDialog datePickerDialog = new DatePickerDialog(dateTextView.getContext(), (view, year1, month1, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dateTextView.setText(date);
        }, year, month, day);

//        Show the dialog
        datePickerDialog.show();
    }

    private void openWalletDialog() {
        // Implement a dialog to select wallet
        String[] walletNames = new String[wallets.size()];
        for (int i = 0; i < wallets.size(); i++) {
            walletNames[i] = wallets.get(i).getName();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("Select Wallet")
                .setItems(walletNames, (dialog, which) -> {
                    selectedWalletId = wallets.get(which).getId();
                    selectedWalletBalance = wallets.get(which).getBalance();
                    walletTextView.setText(walletNames[which]);
                })
                .show();
    }

    private void uploadTransaction() {
        String transactionAmountStr = balanceEditText.getText().toString();
        String category = selectCategoryTextView.getText().toString();
        String note = noteTextView.getText().toString();
        String date = dateTextView.getText().toString();
        String dateToUpload;

        // Check if no date is selected (if dateTextView still shows "Today")
        if (date.equals("Today")) {
            // Get the current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateToUpload = dateFormat.format(new Date()); // Use the current date
        } else {
            dateToUpload = date; // Use the selected date
        }

        if (transactionAmountStr.isEmpty() || category.equals("Select Category") || selectedWalletId == null) {
            // Show error message
            return;
        }

        double transactionAmount = Double.parseDouble(transactionAmountStr);
        double newBalance = selectedWalletBalance;

        // Create transaction data
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("amount", transactionAmount);
        transactionData.put("category", category);
        transactionData.put("note", note);
        transactionData.put("date", dateToUpload);

        String transactionId = UUID.randomUUID().toString();

        String userId = auth.getCurrentUser().getUid(); // Get current user ID


        if(transactionAmount > newBalance){
            Toast.makeText(getContext(), "You don't have sufficient balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("wallets").document(selectedWalletId)
                .collection("transactions").document(transactionId)
                .set(transactionData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Handle successful transaction upload
                        updateWalletBalance(db, userId, selectedWalletId, transactionAmount);
                        resetFields();
                    } else {
                        // Handle error
                    }
                });

    }


    private void updateWalletBalance(FirebaseFirestore db, String userId, String walletId, double amount) {
        // Fetch the current balance from the wallet
        db.collection("users").document(userId)
                .collection("wallets").document(walletId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (selectedWalletBalance != 0) {
                            double newBalance = selectedWalletBalance - amount;

                            // Update the wallet document with the new balance
                            db.collection("users").document(userId)
                                    .collection("wallets").document(walletId)
                                    .update("balance", newBalance)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated the wallet balance
                                        Toast.makeText(getContext(), "Wallet balance updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors
                                        Toast.makeText(getContext(), "Error updating balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors when fetching the wallet document
                    Toast.makeText(getContext(), "Error fetching wallet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void resetFields() {
        balanceEditText.setText("");
        selectCategoryTextView.setText("Select Category");
        noteTextView.setText("Note");
        dateTextView.setText("Today");
        walletTextView.setText("Wallet");
    }

    private static class Wallet {
        private String id;
        private String name;
        private double balance;

        public Wallet(String id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }

        public String getId() {
            return id;
        }

        public double getBalance() {
            return balance;
        }

        public String getName() {
            return name;
        }
    }
}
