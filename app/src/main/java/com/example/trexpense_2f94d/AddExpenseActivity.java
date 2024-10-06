package com.example.trexpense_2f94d;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText expenseNameEditText, expenseAmountEditText;
    private Button saveExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize views
        expenseNameEditText = findViewById(R.id.expenseNameEditText);
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText);
        saveExpenseButton = findViewById(R.id.saveExpenseButton);

        // Save expense and return to HomeActivity
        saveExpenseButton.setOnClickListener(v -> {
            String expenseName = expenseNameEditText.getText().toString();
            String expenseAmount = expenseAmountEditText.getText().toString();

            // Here, you would add code to save this expense (e.g., to a database or a list)

            // After saving, return to the HomeActivity
            finish(); // Ends the AddExpenseActivity and returns to the previous screen
        });
    }
}