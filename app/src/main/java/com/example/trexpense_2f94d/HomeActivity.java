package com.example.trexpense_2f94d;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {
    private Spinner currencySpinner;
    private String selectedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currencySpinner = findViewById(R.id.currencySpinner);

        setupCurrencySpinner();
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
}