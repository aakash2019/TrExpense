package com.example.trexpense_2f94d;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView;
    private TextView userBalanceTextView;
    private Button createWalletButton;
    private Button editWalletButton;


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private double userBalance = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        // Reference to UI components
        usernameTextView = view.findViewById(R.id.usernameTextView);
        userBalanceTextView = view.findViewById(R.id.userBalanceTextView);
        createWalletButton = view.findViewById(R.id.createWalletButton);
        editWalletButton = view.findViewById(R.id.editWalletButton);

        // Fetch and display user information from Firestore
        fetchUserProfile();

        // Handle Create Wallet Button click
        createWalletButton.setOnClickListener(v -> {
            createNewWallet();
        });

        editWalletButton.setOnClickListener(v -> {
            editWalletButton();
        });

        return view;
    }



    private void fetchUserProfile() {
        // Fetching user data from Firestore (assuming the document structure contains 'username' and 'balance')
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                fetchWalletData();

                String name = firstName + " " + lastName;
                // Update UI with fetched data
                usernameTextView.setText(name != null ? name : "No Username");
            }
        }).addOnFailureListener(e -> {
            // Handle errors (could be no internet, Firebase issues, etc.)
            usernameTextView.setText("Error fetching username");
            userBalanceTextView.setText("Error fetching balance");
        });
    }

    private void fetchWalletData() {
        String userId = auth.getCurrentUser().getUid(); // Get current user ID
        userBalance = 0;
        CollectionReference walletsRef = db.collection("users").document(userId).collection("wallets");

        walletsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> walletDocuments = task.getResult().getDocuments();
                double balance = 0;

                for (DocumentSnapshot walletDoc : walletDocuments) {
                    balance = walletDoc.getDouble("balance");

                    // Optionally, you could also accumulate the total balance (in a specific currency if needed)
                    userBalance += balance;

                }
                userBalanceTextView.setText(userBalance != 0 ? "Balance: " + userBalance : "Balance: $0.00");


            } else {
                Log.e("FirestoreError", "Error getting documents: ", task.getException());

            }
        });
    }

    private void createNewWallet() {
        Intent intent = new Intent(getContext(), CreateWalletActivity.class);
        startActivity(intent);
    }

    private void editWalletButton(){
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

//                                  Go to new activity EditWallet
                                    Intent intent = new Intent(getContext(), EditWallet.class);
                                    intent.putExtra("walletName", selectedWalletName);
                                    intent.putExtra("walletId", selectedWalletId);
                                    startActivity(intent);
                                })
                                .show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching wallets", Toast.LENGTH_SHORT).show());
        }

}
