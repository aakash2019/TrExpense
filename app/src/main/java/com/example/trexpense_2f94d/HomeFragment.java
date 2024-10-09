package com.example.trexpense_2f94d;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
public class HomeFragment extends Fragment {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TextView totalBalanceTextView;
    private LinearLayout walletListLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        totalBalanceTextView = view.findViewById(R.id.currentBalanceTextView);
        walletListLayout = view.findViewById(R.id.walletList);

        // Fetch and display wallet data
        fetchWalletData();

        return view;
    }

    private void fetchWalletData() {
        String userId = auth.getCurrentUser().getUid(); // Get current user ID

        CollectionReference walletsRef = firestore.collection("users").document(userId).collection("wallets");

        walletsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> walletDocuments = task.getResult().getDocuments();
                double totalBalance = 0;

                walletListLayout.removeAllViews(); // Clear the layout before adding wallets

                for (DocumentSnapshot walletDoc : walletDocuments) {
                    String walletName = walletDoc.getString("name");
                    double walletBalance = walletDoc.getDouble("balance");
                    String walletCurrency = walletDoc.getString("currency"); // Fetch currency

                    // Add wallet name, balance, and currency to the UI
                    addWalletToLayout(walletName, walletBalance, walletCurrency);

                    // Optionally, you could also accumulate the total balance (in a specific currency if needed)
                    totalBalance += walletBalance;
                }

                // Update the total balance TextView
                totalBalanceTextView.setText(String.format(Locale.getDefault(), "%.2f CAD", totalBalance)); // Assuming total in CAD
            } else {
                Log.e("FirestoreError", "Error getting documents: ", task.getException());
            }
        });
    }

    private void addWalletToLayout(String walletName, double walletBalance, String walletCurrency) {
        // Extract the currency code from the currency string (e.g., "United States Dollar (USD)" -> "USD")
        String currencyCode = walletCurrency.substring(walletCurrency.indexOf("(") + 1, walletCurrency.indexOf(")"));

        LinearLayout walletLayout = new LinearLayout(getContext());
        walletLayout.setOrientation(LinearLayout.HORIZONTAL);
        walletLayout.setWeightSum(2); // Adjust weight sum to 2 for name and balance (with currency)

        // Wallet Name TextView (Left side)
        TextView walletNameTextView = new TextView(getContext());
        walletNameTextView.setText(walletName);
        walletNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        walletNameTextView.setTextSize(16);
        walletNameTextView.setTextColor(Color.BLACK);

        // Wallet Balance TextView (Right side with currency)
        TextView walletBalanceTextView = new TextView(getContext());
        walletBalanceTextView.setText(String.format(Locale.getDefault(), "%.2f %s", walletBalance, currencyCode)); // Balance + Currency (e.g., "1500 USD")
        walletBalanceTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        walletBalanceTextView.setTextSize(16);
        walletBalanceTextView.setTextColor(Color.BLACK);
        walletBalanceTextView.setGravity(Gravity.END); // Align balance to the right

        // Add both views to the horizontal layout
        walletLayout.addView(walletNameTextView);
        walletLayout.addView(walletBalanceTextView);

        // Add the wallet layout to the main wallet list layout
        walletListLayout.addView(walletLayout);
    }
}
