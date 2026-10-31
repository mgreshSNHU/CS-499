package com.example.impetus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Displays and manages user settings.
 */
public class SettingsActivity extends AppCompatActivity {

    // Intent extras
    public static final String EXTRA_OPENED_FROM_SETTINGS =
            "opened_from_settings";

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    // Database and user session
    private DatabaseHelper databaseHelper;
    private long currentUserId;

    // Settings display controls
    private TextView textGoalWeight;
    private TextView textSmsStatus;
    private TextView textProfileSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        databaseHelper = new DatabaseHelper(this);

        currentUserId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1);

        // Prevent access to Settings when no user is logged in.
        if (currentUserId == -1) {
            Intent intent = new Intent(
                    SettingsActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupGoalWeight();
        setupNotificationSettings();
        setupProfile();
        setupLogoutButton();
        setupBottomNavigation();

        updateGoalWeightDisplay();
        updateSmsStatus();
        updateProfileSummary();
    }

    /**
     * Initializes the controls that display current settings.
     */
    private void initializeViews() {
        textGoalWeight =
                findViewById(R.id.textGoalWeight);

        textSmsStatus =
                findViewById(R.id.textSmsStatus);

        textProfileSummary =
                findViewById(R.id.textProfileSummary);
    }

    /**
     * Opens a dialog that allows the user to update their target weight.
     */
    private void setupGoalWeight() {
        findViewById(R.id.cardGoalWeight)
                .setOnClickListener(view -> showGoalWeightDialog());
    }

    /**
     * Displays the goal-weight editing dialog.
     */
    private void showGoalWeightDialog() {
        EditText input = new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        double currentGoal =
                databaseHelper.getGoalWeight(currentUserId);

        if (currentGoal > 0) {
            input.setText(
                    getString(
                            R.string.weight_number_format,
                            currentGoal
                    )
            );

            input.setSelectAllOnFocus(true);
        }

        int padding = getDialogPadding();

        input.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.goal_weight_dialog_title)
                .setMessage(R.string.goal_weight_dialog_message)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(
                        R.string.save,
                        (dialog, which) -> saveGoalWeight(input)
                )
                .show();
    }

    /**
     * Validates and saves the goal weight entered by the user.
     */
    private void saveGoalWeight(EditText input) {
        String value =
                input.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {
            return;
        }

        try {
            double goalWeight =
                    Double.parseDouble(value);

            boolean updated =
                    databaseHelper.updateGoalWeight(
                            currentUserId,
                            goalWeight
                    );

            if (updated) {
                updateGoalWeightDisplay();
            }

        } catch (NumberFormatException ignored) {
            // Invalid numeric input is ignored and no database change is made.
        }
    }

    /**
     * Updates the goal-weight value displayed on the Settings screen.
     */
    private void updateGoalWeightDisplay() {
        double goalWeight =
                databaseHelper.getGoalWeight(currentUserId);

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
     * Configures the SMS notification settings card.
     */
    private void setupNotificationSettings() {
        findViewById(R.id.cardGoalNotifications)
                .setOnClickListener(view -> handleNotificationSelection());
    }

    /**
     * Requests SMS permission when necessary or toggles the
     * notification preference when permission has already been granted.
     */
    private void handleNotificationSelection() {
        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            Intent intent = new Intent(
                    SettingsActivity.this,
                    SmsPermissionActivity.class
            );

            intent.putExtra(
                    EXTRA_OPENED_FROM_SETTINGS,
                    true
            );

            startActivity(intent);
            return;
        }

        boolean smsEnabled =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getBoolean(KEY_SMS_ENABLED, true);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SMS_ENABLED, !smsEnabled)
                .apply();

        updateSmsStatus();
    }

    /**
     * Updates the displayed SMS notification status.
     */
    private void updateSmsStatus() {
        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED;

        boolean smsEnabled =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getBoolean(KEY_SMS_ENABLED, true);

        if (permissionGranted && smsEnabled) {
            textSmsStatus.setText(R.string.sms_enabled);
        } else {
            textSmsStatus.setText(R.string.sms_disabled);
        }
    }

    /**
     * Opens a dialog that allows the user to update account information.
     */
    private void setupProfile() {
        findViewById(R.id.cardProfile)
                .setOnClickListener(view -> showProfileDialog());
    }

    /**
     * Displays the profile editing dialog.
     */
    private void showProfileDialog() {
        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                getDialogPadding();

        container.setPadding(
                padding,
                padding,
                padding,
                0
        );

        EditText usernameInput =
                new EditText(this);

        usernameInput.setHint(
                R.string.username_hint
        );

        usernameInput.setInputType(
                InputType.TYPE_CLASS_TEXT
        );

        EditText phoneInput =
                new EditText(this);

        phoneInput.setHint(
                R.string.phone_number_hint
        );

        phoneInput.setInputType(
                InputType.TYPE_CLASS_PHONE
        );

        String currentUsername =
                databaseHelper.getUsername(currentUserId);

        String currentPhone =
                databaseHelper.getPhoneNumber(currentUserId);

        usernameInput.setText(currentUsername);
        phoneInput.setText(currentPhone);

        container.addView(usernameInput);
        container.addView(phoneInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.profile_dialog_title)
                .setMessage(R.string.profile_dialog_message)
                .setView(container)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(
                        R.string.save,
                        (dialog, which) -> saveProfile(
                                usernameInput,
                                phoneInput
                        )
                )
                .show();
    }

    /**
     * Validates and saves the user's profile information.
     */
    private void saveProfile(
            EditText usernameInput,
            EditText phoneInput
    ) {
        String username =
                usernameInput.getText()
                        .toString()
                        .trim();

        String phoneNumber =
                phoneInput.getText()
                        .toString()
                        .trim();

        if (username.isEmpty()) {
            return;
        }

        boolean usernameUpdated =
                databaseHelper.updateUsername(
                        currentUserId,
                        username
                );

        if (!usernameUpdated) {
            Toast.makeText(
                    SettingsActivity.this,
                    R.string.username_exists,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        databaseHelper.updatePhoneNumber(
                currentUserId,
                phoneNumber
        );

        updateProfileSummary();
    }

    /**
     * Updates the summary shown beneath the Profile setting.
     */
    private void updateProfileSummary() {
        String username =
                databaseHelper.getUsername(currentUserId);

        String phoneNumber =
                databaseHelper.getPhoneNumber(currentUserId);

        if (phoneNumber.isEmpty()) {
            textProfileSummary.setText(username);
        } else {
            textProfileSummary.setText(
                    getString(
                            R.string.profile_summary_format,
                            username,
                            phoneNumber
                    )
            );
        }
    }

    /**
     * Logs the current user out and clears the application task stack.
     */
    private void setupLogoutButton() {
        findViewById(R.id.buttonLogout)
                .setOnClickListener(view -> logoutUser());
    }

    /**
     * Clears the saved user session and returns to LoginActivity.
     */
    private void logoutUser() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(KEY_USER_ID)
                .apply();

        Intent intent = new Intent(
                SettingsActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish();
    }

    /**
     * Configures navigation between the primary application screens.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(
                R.id.navigationSettings
        );

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigationHome) {
                startActivity(
                        new Intent(
                                SettingsActivity.this,
                                DashboardActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationProgress) {
                startActivity(
                        new Intent(
                                SettingsActivity.this,
                                ProgressActivity.class
                        )
                );

                finish();
                return true;
            }

            if (itemId == R.id.navigationHistory) {
                startActivity(
                        new Intent(
                                SettingsActivity.this,
                                HistoryActivity.class
                        )
                );

                finish();
                return true;
            }

            return itemId == R.id.navigationSettings;
        });
    }

    /**
     * Converts 24dp into pixels for dialog padding.
     */
    private int getDialogPadding() {
        return (int) (
                24 * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    /**
     * Refreshes settings that may have changed while another screen
     * was active.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper == null
                || currentUserId == -1) {
            return;
        }

        updateGoalWeightDisplay();
        updateSmsStatus();
        updateProfileSummary();
    }
}