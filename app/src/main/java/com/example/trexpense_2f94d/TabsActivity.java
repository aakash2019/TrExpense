package com.example.trexpense_2f94d;

// Inside your Activity (e.g., MainActivity.java)
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;
import java.util.Map;

public class TabsActivity extends AppCompatActivity {

    private Map<Integer, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Create a map of fragments to avoid switch cases
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.nav_home, new HomeFragment());
        fragmentMap.put(R.id.nav_transactions, new TransactionsFragment());
        fragmentMap.put(R.id.nav_add, new AddFragment());
        fragmentMap.put(R.id.nav_profile, new ProfileFragment());

        // Set HomeFragment as the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragmentMap.get(R.id.nav_home))
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}
