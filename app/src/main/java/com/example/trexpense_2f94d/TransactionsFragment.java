package com.example.trexpense_2f94d;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout transactionContainer;
    private Button btnChooseWallet, btnAddTransaction;
    private ScrollView scrollView;
    private TextView heading;

    private String selectedWalletId = null;
    private String selectedWalletName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Components
        heading = view.findViewById(R.id.tvTransactionsHeading);
        transactionContainer = view.findViewById(R.id.llTransactionContainer);
        btnChooseWallet = view.findViewById(R.id.btnChooseWallet);
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        scrollView = view.findViewById(R.id.svTransactions);

        // Set initial visibility and state
        btnAddTransaction.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);

        // Load current wallet details from Firestore
        loadCurrentWallet();

        // Choose Wallet Button Click Listener
        btnChooseWallet.setOnClickListener(v -> showWalletSelectionDialog());

        // Add Transaction Button Click Listener
        btnAddTransaction.setOnClickListener(v -> navigateToAddTransactionFragment());

        return view;
    }

    private void loadCurrentWallet() {
        String userId = auth.getCurrentUser().getUid();
        // Fetch the user document
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot userDoc = task.getResult();
                        // Get the currentWallet id from the user document
                        selectedWalletId = userDoc.getString("currentWallet");

                        if (selectedWalletId != null) {
                            // Fetch wallet details using the currentWallet ID
                            fetchWalletDetails(selectedWalletId);
                        } else {
                            Toast.makeText(getContext(), "No current wallet set", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreError", "Error fetching user data: ", task.getException());
                        Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchWalletDetails(String walletId) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("wallets").document(walletId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot walletDoc = task.getResult();
                        selectedWalletName = walletDoc.getString("name");

                        if (selectedWalletName != null) {
                            heading.setText("Transactions (" + selectedWalletName + ")");
                            fetchTransactionsForWallet(walletId);
                        } else {
                            Toast.makeText(getContext(), "Wallet not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreError", "Error fetching wallet details: ", task.getException());
                        Toast.makeText(getContext(), "Error loading wallet details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showWalletSelectionDialog() {
        String userId = auth.getCurrentUser().getUid();
        CollectionReference walletsRef = db.collection("users").document(userId).collection("wallets");

        walletsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> walletDocuments = task.getResult().getDocuments();
                if (!walletDocuments.isEmpty()) {
                    List<String> walletNames = new ArrayList<>();
                    List<String> walletIds = new ArrayList<>();
                    for (DocumentSnapshot walletDoc : walletDocuments) {
                        String walletName = walletDoc.getString("name");
                        walletNames.add(walletName != null ? walletName : "Unnamed Wallet");
                        walletIds.add(walletDoc.getId());
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Choose Wallet")
                            .setItems(walletNames.toArray(new String[0]), (dialog, which) -> {
                                selectedWalletName = walletNames.get(which);
                                selectedWalletId = walletIds.get(which);
                                heading.setText("Transactions (" + selectedWalletName + ")");
                                fetchTransactionsForWallet(selectedWalletId);

                                // Update the current wallet in Firestore
                                updateCurrentWallet(selectedWalletId);
                            })
                            .show();
                } else {
                    Toast.makeText(getContext(), "No wallets found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("FirestoreError", "Error fetching wallets: ", task.getException());
                Toast.makeText(getContext(), "Error loading wallets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentWallet(String newWalletId) {
        String userId = auth.getCurrentUser().getUid();
        // Update the currentWallet field directly in the user's document
        db.collection("users").document(userId)
                .update("currentWallet", newWalletId)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Current wallet updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error updating current wallet: ", e);
                    Toast.makeText(getContext(), "Error updating current wallet", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTransactionsForWallet(String walletId) {
        if (walletId == null) {
            Toast.makeText(getContext(), "No wallet selected", Toast.LENGTH_SHORT).show();
            return;
        }

        transactionContainer.removeAllViews();
        scrollView.setVisibility(View.VISIBLE);
        btnAddTransaction.setVisibility(View.GONE);

        String userId = auth.getCurrentUser().getUid();
        CollectionReference transactionsRef = db.collection("users").document(userId)
                .collection("wallets").document(walletId).collection("transactions");

        transactionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (!task.getResult().isEmpty()) {
                    for (DocumentSnapshot transactionDoc : task.getResult()) {
                        addTransactionToUI(transactionDoc);
                    }
                } else {
                    btnAddTransaction.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e("FirestoreError", "Error fetching transactions: ", task.getException());
                Toast.makeText(getContext(), "Error loading transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTransactionToUI(DocumentSnapshot transactionDoc) {
        String note = transactionDoc.getString("note");
        double amount = transactionDoc.contains("amount") ? transactionDoc.getDouble("amount") : 0.0;
        String date = transactionDoc.getString("date");
        String category = transactionDoc.getString("category");

        View transactionRow = LayoutInflater.from(getContext()).inflate(R.layout.transaction_row, transactionContainer, false);

        TextView noteTextView = transactionRow.findViewById(R.id.tvTransactionNote);
        TextView amountTextView = transactionRow.findViewById(R.id.tvTransactionAmount);
        TextView dateTextView = transactionRow.findViewById(R.id.tvTransactionDate);
        TextView categoryTextView = transactionRow.findViewById(R.id.tvTransactionCategory);

        noteTextView.setText(note != null ? note : "No Note");
        amountTextView.setText(String.format("$%.2f", amount));
        dateTextView.setText(date != null ? date : "No Date");
        categoryTextView.setText(category != null ? category : "No Category");

        transactionContainer.addView(transactionRow);
    }

    private void navigateToAddTransactionFragment() {
        Fragment addFragment = new AddFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
