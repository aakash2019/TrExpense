package com.example.trexpense_2f94d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView walletNameTextView, balanceTextView;
    private ListView expensesListView;
    private Button addExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        walletNameTextView = findViewById(R.id.walletNameTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        expensesListView = findViewById(R.id.expensesListView);
        addExpenseButton = findViewById(R.id.addExpenseButton);

        // Retrieve wallet data from the intent
        Intent intent = getIntent();
        String walletName = intent.getStringExtra("walletName");
        String balance = intent.getStringExtra("balance");

        // Set wallet information
        walletNameTextView.setText("Wallet: " + walletName);
        balanceTextView.setText("$" + balance);

        // Add Expense button click listener
        addExpenseButton.setOnClickListener(v -> {
            // Open activity to add a new expense
            Intent addExpenseIntent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(addExpenseIntent);
        });

        // Set up list view for expenses (you'll add actual expenses later)
        ArrayList<String> expenses = new ArrayList<>();
        // Dummy data for now
        expenses.add("Expense 1: $10");
        expenses.add("Expense 2: $5");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenses);
        expensesListView.setAdapter(adapter);
    }
}