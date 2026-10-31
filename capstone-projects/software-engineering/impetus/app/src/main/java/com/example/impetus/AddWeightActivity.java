package com.example.impetus;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Allows the user to create a new weigh-in or edit an existing weigh-in.
 * The activity stores weight, date, time, mood, and notes in the local
 * SQLite database. When a new weigh-in reaches the user's goal weight,
 * the activity can also send an SMS notification if the user has enabled
 * notifications and granted SMS permission.
 */
public class AddWeightActivity extends AppCompatActivity {

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    // Form controls
    private TextInputEditText editDate;
    private TextInputEditText editTime;
    private TextView textMoodValue;

    // Database and user session
    private DatabaseHelper databaseHelper;
    private long currentUserId;

    // Edit-mode state
    private boolean editMode = false;
    private long weighInId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        databaseHelper = new DatabaseHelper(this);

        currentUserId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_USER_ID, -1);

        // Prevent access to the screen when no user is logged in.
        if (currentUserId == -1) {
            Intent intent = new Intent(
                    AddWeightActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
            return;
        }

        Intent intent = getIntent();

        editMode = intent.getBooleanExtra("EDIT_MODE", false);

        if (editMode) {
            weighInId = intent.getLongExtra("WEIGH_IN_ID", -1);
        }

        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        textMoodValue = findViewById(R.id.textMoodValue);

        setupNotesScrolling();
        setupMoodSlider();
        populateExistingEntry();
        setupToolbar();
        setupDatePicker();
        setupTimePicker();
        setupButtons();
    }

    /**
     * Keeps the notes field visible when the keyboard opens.
     */
    private void setupNotesScrolling() {
        TextInputEditText editNotes = findViewById(R.id.editNotes);
        NestedScrollView scrollView = findViewById(R.id.addWeightScrollView);

        editNotes.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(
                        () -> scrollView.smoothScrollTo(
                                0,
                                editNotes.getBottom()
                        ),
                        250
                );
            }
        });
    }

    /**
     * Configures the toolbar navigation button to close the screen.
     */
    private void setupToolbar() {
        MaterialToolbar toolbar =
                findViewById(R.id.addWeightToolbar);

        toolbar.setNavigationOnClickListener(view -> finish());
    }

    /**
     * Initializes the date field and displays a date picker when selected.
     */
    private void setupDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (!editMode) {
            editDate.setText(
                    DateFormat.getDateInstance(DateFormat.MEDIUM)
                            .format(calendar.getTime())
            );
        }

        editDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (datePicker, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();

                        selectedDate.set(
                                year,
                                month,
                                dayOfMonth
                        );

                        editDate.setText(
                                DateFormat.getDateInstance(DateFormat.MEDIUM)
                                        .format(selectedDate.getTime())
                        );
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });
    }

    /**
     * Initializes the time field and displays a time picker when selected.
     */
    private void setupTimePicker() {
        Calendar calendar = Calendar.getInstance();

        if (!editMode) {
            editTime.setText(
                    DateFormat.getTimeInstance(DateFormat.SHORT)
                            .format(calendar.getTime())
            );
        }

        editTime.setOnClickListener(view -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (timePicker, hourOfDay, minute) -> {
                        Calendar selectedTime = Calendar.getInstance();

                        selectedTime.set(
                                Calendar.HOUR_OF_DAY,
                                hourOfDay
                        );

                        selectedTime.set(
                                Calendar.MINUTE,
                                minute
                        );

                        editTime.setText(
                                DateFormat.getTimeInstance(DateFormat.SHORT)
                                        .format(selectedTime.getTime())
                        );
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            dialog.show();
        });
    }

    /**
     * Updates the displayed mood value as the slider changes.
     */
    private void setupMoodSlider() {
        Slider sliderMood = findViewById(R.id.sliderMood);

        sliderMood.addOnChangeListener(
                (slider, value, fromUser) -> {
                    int moodValue = Math.round(value);

                    textMoodValue.setText(
                            getString(
                                    R.string.mood_value_format,
                                    moodValue
                            )
                    );
                }
        );
    }

    /**
     * Populates the form with the existing values when editing a weigh-in.
     */
    private void populateExistingEntry() {
        if (!editMode) {
            return;
        }

        TextInputEditText editWeight = findViewById(R.id.editWeight);
        TextInputEditText editNotes = findViewById(R.id.editNotes);
        Slider sliderMood = findViewById(R.id.sliderMood);

        Intent intent = getIntent();

        double existingWeight =
                intent.getDoubleExtra("WEIGHT", 0.0);

        String existingDate =
                intent.getStringExtra("DATE");

        String existingTime =
                intent.getStringExtra("TIME");

        int existingMood =
                intent.getIntExtra("MOOD", 5);

        String existingNotes =
                intent.getStringExtra("NOTES");

        editWeight.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f",
                        existingWeight
                )
        );

        if (existingDate != null) {
            editDate.setText(existingDate);
        }

        if (existingTime != null) {
            editTime.setText(existingTime);
        }

        sliderMood.setValue(existingMood);

        if (existingNotes != null) {
            editNotes.setText(existingNotes);
        }
    }

    /**
     * Configures the Cancel and Save buttons for the weigh-in form.
     */
    private void setupButtons() {
        findViewById(R.id.buttonCancel)
                .setOnClickListener(view -> finish());

        findViewById(R.id.buttonSaveWeight)
                .setOnClickListener(view -> saveWeighIn());
    }

    /**
     * Validates the form and either creates a new database record or
     * updates the existing weigh-in.
     */
    private void saveWeighIn() {
        TextInputEditText editWeight = findViewById(R.id.editWeight);
        TextInputEditText editNotes = findViewById(R.id.editNotes);
        Slider sliderMood = findViewById(R.id.sliderMood);

        String weightText = editWeight.getText() == null
                ? ""
                : editWeight.getText().toString().trim();

        if (weightText.isEmpty()) {
            editWeight.setError(
                    getString(R.string.weight_required)
            );
            editWeight.requestFocus();
            return;
        }

        double weight;

        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException exception) {
            editWeight.setError(
                    getString(R.string.invalid_weight)
            );
            editWeight.requestFocus();
            return;
        }

        String date = editDate.getText() == null
                ? ""
                : editDate.getText().toString();

        String time = editTime.getText() == null
                ? ""
                : editTime.getText().toString();

        int mood = Math.round(sliderMood.getValue());

        String notes = editNotes.getText() == null
                ? ""
                : editNotes.getText().toString().trim();

        if (editMode) {
            updateExistingWeighIn(
                    weight,
                    date,
                    time,
                    mood,
                    notes
            );
        } else {
            createNewWeighIn(
                    weight,
                    date,
                    time,
                    mood,
                    notes
            );
        }
    }

    /**
     * Updates an existing weigh-in in the SQLite database.
     */
    private void updateExistingWeighIn(
            double weight,
            String date,
            String time,
            int mood,
            String notes
    ) {
        boolean updated = databaseHelper.updateWeighIn(
                weighInId,
                currentUserId,
                weight,
                date,
                time,
                mood,
                notes
        );

        if (updated) {
            Toast.makeText(
                    AddWeightActivity.this,
                    R.string.weigh_in_updated,
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    AddWeightActivity.this,
                    R.string.weigh_in_update_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Saves a new weigh-in and checks whether it triggered the user's goal.
     */
    private void createNewWeighIn(
            double weight,
            String date,
            String time,
            int mood,
            String notes
    ) {
        long result = databaseHelper.addWeighIn(
                currentUserId,
                weight,
                date,
                time,
                mood,
                notes
        );

        if (result != -1) {
            sendGoalSmsIfNeeded(weight);

            Toast.makeText(
                    AddWeightActivity.this,
                    R.string.weigh_in_saved,
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    AddWeightActivity.this,
                    R.string.weigh_in_save_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Sends an SMS notification when a new weigh-in reaches the user's
     * target weight and all required SMS settings are enabled.
     */
    @SuppressWarnings("deprecation")
    private void sendGoalSmsIfNeeded(double weight) {
        boolean smsEnabled =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getBoolean(KEY_SMS_ENABLED, true);

        if (!smsEnabled) {
            return;
        }

        double goalWeight =
                databaseHelper.getGoalWeight(currentUserId);

        if (goalWeight <= 0 || weight > goalWeight) {
            return;
        }

        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            return;
        }

        String phoneNumber =
                databaseHelper.getPhoneNumber(currentUserId);

        if (phoneNumber.isEmpty()) {
            return;
        }

        String message = getString(
                R.string.goal_reached_sms,
                goalWeight
        );

        SmsManager smsManager =
                SmsManager.getDefault();

        smsManager.sendTextMessage(
                phoneNumber,
                null,
                message,
                null,
                null
        );
    }
}