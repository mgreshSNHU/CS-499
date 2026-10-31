package com.example.impetus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Displays the main Impetus dashboard.
 */
public class DashboardActivity extends AppCompatActivity {

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";

    // Dashboard data
    private final List<WeighInEntry> weighInEntries =
            new ArrayList<>();

    private WeighInAdapter weighInAdapter;
    private DatabaseHelper databaseHelper;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);

        currentUserId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1);

        // Return to login if no authenticated user is available.
        if (currentUserId == -1) {
            Intent intent = new Intent(
                    DashboardActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        setupRecentWeighIns();
        setupAddWeightButton();
        setupSeeAllButton();
        setupProfileButton();
        setupBottomNavigation();

        updateUserProfile();
        updateDashboardSummary();
    }

    /**
     * Configures the RecyclerView that displays the three most recent
     * weigh-ins on the dashboard.
     */
    private void setupRecentWeighIns() {
        RecyclerView recyclerView =
                findViewById(R.id.recyclerRecentWeighIns);

        refreshRecentWeighIns();

        weighInAdapter = new WeighInAdapter(
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

        recyclerView.setAdapter(weighInAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * Reloads the three most recent weigh-ins from the database.
     */
    private void refreshRecentWeighIns() {
        weighInEntries.clear();

        List<WeighInEntry> allEntries =
                databaseHelper.getWeighInsForUser(currentUserId);

        int limit = Math.min(3, allEntries.size());

        weighInEntries.addAll(
                allEntries.subList(0, limit)
        );
    }

    /**
     * Opens an existing weigh-in in AddWeightActivity for editing.
     */
    private void openWeighInForEdit(WeighInEntry entry) {
        Intent intent = new Intent(
                DashboardActivity.this,
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
     * Deletes a weigh-in and refreshes the dashboard summary.
     */
    private void deleteWeighIn(int position) {
        WeighInEntry entry = weighInEntries.get(position);

        boolean deleted = databaseHelper.deleteWeighIn(
                entry.getId(),
                currentUserId
        );

        if (deleted) {
            weighInEntries.remove(position);
            weighInAdapter.notifyItemRemoved(position);

            updateDashboardSummary();
        }
    }

    /**
     * Opens the Add Weight screen.
     */
    private void setupAddWeightButton() {
        findViewById(R.id.fabAddWeight)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(
                            DashboardActivity.this,
                            AddWeightActivity.class
                    );

                    startActivity(intent);
                });
    }

    /**
     * Opens the complete weigh-in history.
     */
    private void setupSeeAllButton() {
        findViewById(R.id.buttonSeeAll)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(
                            DashboardActivity.this,
                            HistoryActivity.class
                    );

                    startActivity(intent);
                });
    }

    /**
     * Opens Settings when the profile avatar is selected.
     */
    private void setupProfileButton() {
        findViewById(R.id.profileAvatar)
                .setOnClickListener(view -> {
                    Intent intent = new Intent(
                            DashboardActivity.this,
                            SettingsActivity.class
                    );

                    startActivity(intent);
                });
    }

    /**
     * Configures navigation between the primary application screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(
                R.id.navigationHome
        );

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigationHome) {
                return true;
            }

            if (itemId == R.id.navigationProgress) {
                startActivity(
                        new Intent(
                                DashboardActivity.this,
                                ProgressActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationHistory) {
                startActivity(
                        new Intent(
                                DashboardActivity.this,
                                HistoryActivity.class
                        )
                );

                return true;
            }

            if (itemId == R.id.navigationSettings) {
                startActivity(
                        new Intent(
                                DashboardActivity.this,
                                SettingsActivity.class
                        )
                );

                return true;
            }

            return false;
        });
    }

    /**
     * Updates all database-driven statistics displayed on the dashboard.
     */
    private void updateDashboardSummary() {
        TextView textCurrentWeight =
                findViewById(R.id.textDashboardCurrentWeight);

        TextView textGoalWeight =
                findViewById(R.id.textDashboardGoalWeight);

        TextView textRemainingWeight =
                findViewById(R.id.textDashboardRemainingWeight);

        TextView textProgressPercent =
                findViewById(R.id.textDashboardProgressPercent);

        TextView textWeeklyChange =
                findViewById(R.id.textDashboardWeeklyChange);

        TextView textTrackingStreak =
                findViewById(R.id.textDashboardTrackingStreak);

        TextView textMood =
                findViewById(R.id.textDashboardMood);

        CircularProgressIndicator circularProgress =
                findViewById(R.id.circularGoalProgress);

        LinearProgressIndicator linearProgress =
                findViewById(R.id.linearGoalProgress);

        List<WeighInEntry> entries =
                databaseHelper.getWeighInsForUser(currentUserId);

        double goalWeight =
                databaseHelper.getGoalWeight(currentUserId);

        updateGoalReachedBanner(entries);

        if (entries.isEmpty()) {
            textCurrentWeight.setText(R.string.not_available);
            textRemainingWeight.setText(R.string.not_available);
            textMood.setText(R.string.not_available);
            textWeeklyChange.setText(R.string.not_available);
            textTrackingStreak.setText(R.string.not_available);
            textProgressPercent.setText(R.string.zero_percent);

            circularProgress.setProgress(0);
            linearProgress.setProgress(0);
        } else {
            WeighInEntry currentEntry = entries.get(0);

            double currentWeight =
                    currentEntry.getWeight();

            double weeklyChange =
                    calculateWeeklyChange(entries);

            int trackingStreak =
                    calculateTrackingStreak(entries);

            textCurrentWeight.setText(
                    getString(
                            R.string.weight_number_format,
                            currentWeight
                    )
            );

            textMood.setText(
                    getString(
                            R.string.mood_score_format,
                            currentEntry.getMood()
                    )
            );

            textWeeklyChange.setText(
                    getString(
                            R.string.weight_change_format,
                            weeklyChange
                    )
            );

            textTrackingStreak.setText(
                    getResources().getQuantityString(
                            R.plurals.tracking_streak_days,
                            trackingStreak,
                            trackingStreak
                    )
            );

            updateGoalProgress(
                    entries,
                    currentWeight,
                    goalWeight,
                    textRemainingWeight,
                    textProgressPercent,
                    circularProgress,
                    linearProgress
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
     * Calculates and displays progress between the starting weight
     * and the user's goal weight.
     */
    private void updateGoalProgress(
            List<WeighInEntry> entries,
            double currentWeight,
            double goalWeight,
            TextView textRemainingWeight,
            TextView textProgressPercent,
            CircularProgressIndicator circularProgress,
            LinearProgressIndicator linearProgress
    ) {
        if (goalWeight <= 0) {
            textRemainingWeight.setText(R.string.not_available);
            textProgressPercent.setText(R.string.zero_percent);

            circularProgress.setProgress(0);
            linearProgress.setProgress(0);
            return;
        }

        double remaining =
                Math.max(0, currentWeight - goalWeight);

        textRemainingWeight.setText(
                getString(
                        R.string.weight_with_unit_format,
                        remaining
                )
        );

        WeighInEntry startingEntry =
                entries.get(entries.size() - 1);

        double startingWeight =
                startingEntry.getWeight();

        double totalNeeded =
                startingWeight - goalWeight;

        double lostSoFar =
                startingWeight - currentWeight;

        int progress = 0;

        if (totalNeeded > 0) {
            progress = (int) Math.round(
                    (lostSoFar / totalNeeded) * 100
            );

            // Keep the displayed progress within the valid 0–100 range.
            progress = Math.max(
                    0,
                    Math.min(100, progress)
            );
        }

        circularProgress.setProgress(progress);
        linearProgress.setProgress(progress);

        textProgressPercent.setText(
                getString(
                        R.string.percent_format,
                        progress
                )
        );
    }

    /**
     * Shows the achievement banner when the user's latest weight
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
     * Loads the user's name and generates initials for the profile avatar.
     */
    private void updateUserProfile() {
        TextView textUsername =
                findViewById(R.id.textDashboardUsername);

        TextView textProfileInitials =
                findViewById(R.id.textProfileInitials);

        String username =
                databaseHelper.getUsername(currentUserId);

        if (username.isEmpty()) {
            textUsername.setText(R.string.welcome);
            textProfileInitials.setText(R.string.unknown_initial);
            return;
        }

        textUsername.setText(username);

        String[] parts =
                username.trim().split("\\s+");

        String initials;

        if (parts.length >= 2) {
            initials =
                    (
                            parts[0].charAt(0)
                                    + parts[parts.length - 1]
                                    .substring(0, 1)
                    ).toUpperCase(Locale.getDefault());
        } else {
            initials =
                    username.substring(0, 1)
                            .toUpperCase(Locale.getDefault());
        }

        textProfileInitials.setText(initials);
    }

    /**
     * Calculates the user's weight change over approximately seven days.
     */
    private double calculateWeeklyChange(
            List<WeighInEntry> entries
    ) {
        if (entries.size() < 2) {
            return 0.0;
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "MMM d, yyyy",
                        Locale.getDefault()
                );

        Date newestDate;

        try {
            newestDate =
                    format.parse(entries.get(0).getDate());
        } catch (ParseException exception) {
            return 0.0;
        }

        if (newestDate == null) {
            return 0.0;
        }

        WeighInEntry newestEntry =
                entries.get(0);

        WeighInEntry comparisonEntry = null;

        for (WeighInEntry entry : entries) {
            try {
                Date entryDate =
                        format.parse(entry.getDate());

                if (entryDate == null) {
                    continue;
                }

                long differenceMs =
                        newestDate.getTime()
                                - entryDate.getTime();

                long differenceDays =
                        TimeUnit.MILLISECONDS.toDays(
                                differenceMs
                        );

                if (differenceDays >= 7) {
                    comparisonEntry = entry;
                    break;
                }

            } catch (ParseException ignored) {
                // Ignore malformed dates and continue searching.
            }
        }

        if (comparisonEntry == null) {
            comparisonEntry =
                    entries.get(entries.size() - 1);
        }

        return newestEntry.getWeight()
                - comparisonEntry.getWeight();
    }

    /**
     * Calculates the number of consecutive calendar days containing
     * at least one weigh-in.
     */
    private int calculateTrackingStreak(
            List<WeighInEntry> entries
    ) {
        if (entries.isEmpty()) {
            return 0;
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "MMM d, yyyy",
                        Locale.getDefault()
                );

        Set<Long> uniqueDays =
                new HashSet<>();

        for (WeighInEntry entry : entries) {
            try {
                Date date =
                        format.parse(entry.getDate());

                if (date != null) {
                    uniqueDays.add(
                            TimeUnit.MILLISECONDS.toDays(
                                    date.getTime()
                            )
                    );
                }

            } catch (ParseException ignored) {
                // Ignore malformed dates when calculating the streak.
            }
        }

        if (uniqueDays.isEmpty()) {
            return 0;
        }

        long mostRecentDay =
                Collections.max(uniqueDays);

        int streak = 0;
        long day = mostRecentDay;

        while (uniqueDays.contains(day)) {
            streak++;
            day--;
        }

        return streak;
    }

    /**
     * Reloads database-backed content whenever the user returns
     * to the dashboard.
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper == null
                || currentUserId == -1
                || weighInAdapter == null) {
            return;
        }

        refreshRecentWeighIns();
        weighInAdapter.notifyDataSetChanged();

        updateDashboardSummary();
        updateUserProfile();
    }
}