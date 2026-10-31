package com.example.impetus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Displays the user's weight-tracking progress.
 */
public class ProgressActivity extends AppCompatActivity {

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";

    // Database and user session
    private DatabaseHelper databaseHelper;
    private long currentUserId;

    // Progress summary controls
    private TextView textStartingWeight;
    private TextView textCurrentWeight;
    private TextView textGoalWeight;
    private TextView textTotalChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        currentUserId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1);

        // Prevent access to the progress screen when no user is logged in.
        if (currentUserId == -1) {
            Intent intent = new Intent(
                    ProgressActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupBottomNavigation();
        loadProgressData();
    }

    /**
     * Initializes the controls used to display progress statistics.
     */
    private void initializeViews() {
        textStartingWeight =
                findViewById(R.id.textStartingWeight);

        textCurrentWeight =
                findViewById(R.id.textCurrentWeight);

        textGoalWeight =
                findViewById(R.id.textProgressGoalWeight);

        textTotalChange =
                findViewById(R.id.textTotalChange);
    }

    /**
     * Loads the user's weigh-ins and updates all progress information.
     */
    private void loadProgressData() {
        List<WeighInEntry> entries =
                databaseHelper.getWeighInsForUser(currentUserId);

        double goalWeight =
                databaseHelper.getGoalWeight(currentUserId);

        updateWeightChart(entries);
        updateGoalReachedBanner(entries);
        updateWeightSummary(entries, goalWeight);
    }

    /**
     * Updates the four summary values displayed below the chart.
     */
    private void updateWeightSummary(
            List<WeighInEntry> entries,
            double goalWeight
    ) {
        if (entries.isEmpty()) {
            textStartingWeight.setText(R.string.not_available);
            textCurrentWeight.setText(R.string.not_available);
            textTotalChange.setText(R.string.not_available);
        } else {
            WeighInEntry currentEntry =
                    entries.get(0);

            WeighInEntry startingEntry =
                    entries.get(entries.size() - 1);

            double startingWeight =
                    startingEntry.getWeight();

            double currentWeight =
                    currentEntry.getWeight();

            double totalChange =
                    currentWeight - startingWeight;

            textStartingWeight.setText(
                    getString(
                            R.string.weight_with_unit_format,
                            startingWeight
                    )
            );

            textCurrentWeight.setText(
                    getString(
                            R.string.weight_with_unit_format,
                            currentWeight
                    )
            );

            textTotalChange.setText(
                    getString(
                            R.string.weight_change_format,
                            totalChange
                    )
            );
        }

        if (goalWeight > 0) {
            textGoalWeight.setText(
                    getString(
                            R.string.weight_with_unit_format,
                            goalWeight
                    )
            );
        } else {
            textGoalWeight.setText(R.string.not_set);
        }
    }

    /**
     * Supplies the user's weight history to the custom line chart.
     */
    private void updateWeightChart(
            List<WeighInEntry> entries
    ) {
        WeightChartView weightChart =
                findViewById(R.id.weightChart);

        List<Double> weights =
                new ArrayList<>();

        for (WeighInEntry entry : entries) {
            weights.add(entry.getWeight());
        }

        Collections.reverse(weights);

        weightChart.setWeights(weights);
    }

    /**
     * Displays the achievement banner when the user's latest weight
     * is at or below the configured goal weight.
     */
    private void updateGoalReachedBanner(
            List<WeighInEntry> entries
    ) {
        View goalReachedCard =
                findViewById(R.id.cardGoalReached);

        double goalWeight =
                databaseHelper.getGoalWeight(currentUserId);

        boolean goalReached =
                !entries.isEmpty()
                        && goalWeight > 0
                        && entries.get(0).getWeight() <= goalWeight;

        goalReachedCard.setVisibility(
                goalReached ? View.VISIBLE : View.GONE
        );
    }

    /**
     * Configures navigation between the primary application screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(
                R.id.navigationProgress
        );

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigationHome) {
                startActivity(
                        new Intent(
                                ProgressActivity.this,
                                DashboardActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationProgress) {
                return true;
            }

            if (itemId == R.id.navigationHistory) {
                startActivity(
                        new Intent(
                                ProgressActivity.this,
                                HistoryActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationSettings) {
                startActivity(
                        new Intent(
                                ProgressActivity.this,
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
     * Refreshes progress information whenever the user returns
     * to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null
                && currentUserId != -1) {
            loadProgressData();
        }
    }
}