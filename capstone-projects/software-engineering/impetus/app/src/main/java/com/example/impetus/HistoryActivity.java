package com.example.impetus;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the user's complete weigh-in history.
 */
public class HistoryActivity extends AppCompatActivity {

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";

    // History data
    private final List<WeighInEntry> weighInEntries =
            new ArrayList<>();

    private DatabaseHelper databaseHelper;
    private HistoryAdapter historyAdapter;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        currentUserId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1);

        // Prevent access to the history screen when no user is logged in.
        if (currentUserId == -1) {
            Intent intent = new Intent(
                    HistoryActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        setupRecyclerView();
        setupBottomNavigation();
    }

    /**
     * Configures the RecyclerView used to display all saved weigh-ins.
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView =
                findViewById(R.id.recyclerHistory);

        refreshHistory();

        historyAdapter = new HistoryAdapter(
                weighInEntries,

                // Open the selected record in edit mode.
                position -> openWeighInForEdit(
                        weighInEntries.get(position)
                ),

                // Delete the selected record from SQLite.
                this::deleteWeighIn
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(historyAdapter);
    }

    /**
     * Reloads all weigh-ins belonging to the current user.
     */
    private void refreshHistory() {
        weighInEntries.clear();

        weighInEntries.addAll(
                databaseHelper.getWeighInsForUser(currentUserId)
        );
    }

    /**
     * Opens an existing weigh-in in AddWeightActivity for editing.
     */
    private void openWeighInForEdit(WeighInEntry entry) {
        Intent intent = new Intent(
                HistoryActivity.this,
                AddWeightActivity.class
        );

        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("WEIGH_IN_ID", entry.getId());
        intent.putExtra("WEIGHT", entry.getWeight());
        intent.putExtra("DATE", entry.getDate());
        intent.putExtra("TIME", entry.getTime());
        intent.putExtra("MOOD", entry.getMood());
        intent.putExtra("NOTES", entry.getNotes());

        startActivity(intent);
    }

    /**
     * Deletes a weigh-in from the database and updates the displayed list.
     */
    private void deleteWeighIn(int position) {
        WeighInEntry entry =
                weighInEntries.get(position);

        boolean deleted =
                databaseHelper.deleteWeighIn(
                        entry.getId(),
                        currentUserId
                );

        if (deleted) {
            weighInEntries.remove(position);
            historyAdapter.notifyItemRemoved(position);
        }
    }

    /**
     * Configures navigation between the primary application screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(
                R.id.navigationHistory
        );

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigationHome) {
                startActivity(
                        new Intent(
                                HistoryActivity.this,
                                DashboardActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationProgress) {
                startActivity(
                        new Intent(
                                HistoryActivity.this,
                                ProgressActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationHistory) {
                return true;
            }

            if (itemId == R.id.navigationSettings) {
                startActivity(
                        new Intent(
                                HistoryActivity.this,
                                SettingsActivity.class
                        )
                );

                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Refreshes the history whenever the user returns from another screen,
     * such as after editing a weigh-in.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper == null
                || currentUserId == -1
                || historyAdapter == null) {
            return;
        }

        refreshHistory();
        historyAdapter.notifyDataSetChanged();
    }
}