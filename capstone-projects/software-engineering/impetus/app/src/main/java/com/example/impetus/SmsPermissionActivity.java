package com.example.impetus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Handles the optional SMS permission flow.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    // Screen state
    private boolean openedFromSettings;

    // Permission status controls
    private TextView textPermissionStatus;
    private ImageView imagePermissionStatus;
    private MaterialButton buttonAllowSms;
    private MaterialButton buttonNotNow;

    /**
     * Receives the result of the Android SMS permission request.
     */
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            showPermissionGranted();
                        } else {
                            showPermissionDenied();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms_permission);

        openedFromSettings =
                getIntent().getBooleanExtra(
                        SettingsActivity.EXTRA_OPENED_FROM_SETTINGS,
                        false
                );

        setupWindowInsets();
        initializeViews();
        setupButtons();
        updatePermissionStatus();
    }

    /**
     * Applies system bar spacing when edge-to-edge display is enabled.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    /**
     * Initializes the controls used by the permission screen.
     */
    private void initializeViews() {
        textPermissionStatus =
                findViewById(R.id.textPermissionStatus);

        imagePermissionStatus =
                findViewById(R.id.imagePermissionStatus);

        buttonAllowSms =
                findViewById(R.id.buttonAllowSms);

        buttonNotNow =
                findViewById(R.id.buttonNotNow);
    }

    /**
     * Configures the initial button actions.
     */
    private void setupButtons() {
        buttonAllowSms.setOnClickListener(
                view -> requestSmsPermission()
        );

        buttonNotNow.setOnClickListener(
                view -> showPermissionDenied()
        );
    }

    /**
     * Evaluates telephony support and the current SMS permission state.
     */
    private void updatePermissionStatus() {
        boolean hasTelephony =
                getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_TELEPHONY
                );

        if (!hasTelephony) {
            showSmsUnavailable();
            return;
        }

        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {
            showPermissionGranted();
        } else {
            showPermissionNotRequested();
        }
    }

    /**
     * Requests SEND_SMS permission from Android.
     */
    private void requestSmsPermission() {
        smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
        );
    }

    /**
     * Displays the state shown before SMS permission has been granted.
     */
    private void showPermissionNotRequested() {
        textPermissionStatus.setText(
                R.string.sms_status_not_requested
        );

        imagePermissionStatus.setImageResource(
                R.drawable.ic_notifications_off
        );

        buttonAllowSms.setText(
                R.string.allow_sms
        );

        buttonAllowSms.setOnClickListener(
                view -> requestSmsPermission()
        );

        buttonAllowSms.setVisibility(View.VISIBLE);
        buttonNotNow.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the state shown when SMS permission has been granted.
     */
    private void showPermissionGranted() {
        textPermissionStatus.setText(
                R.string.sms_status_granted
        );

        imagePermissionStatus.setImageResource(
                R.drawable.ic_check_circle
        );

        showContinueState();
    }

    /**
     * Displays the state shown when SMS permission has been denied.
     */
    private void showPermissionDenied() {
        textPermissionStatus.setText(
                R.string.sms_status_denied
        );

        imagePermissionStatus.setImageResource(
                R.drawable.ic_notifications_off
        );

        showContinueState();
    }

    /**
     * Displays the state shown when the device does not support telephony.
     */
    private void showSmsUnavailable() {
        textPermissionStatus.setText(
                R.string.sms_status_unavailable
        );

        imagePermissionStatus.setImageResource(
                R.drawable.ic_notifications_off
        );

        showContinueState();
    }

    /**
     * Changes the permission controls into a single Continue action.
     */
    private void showContinueState() {
        buttonAllowSms.setText(
                R.string.continue_to_app
        );

        buttonAllowSms.setOnClickListener(
                view -> continueFromPermissionScreen()
        );

        buttonAllowSms.setVisibility(View.VISIBLE);
        buttonNotNow.setVisibility(View.GONE);
    }

    /**
     * Returns to Settings or continues to the Dashboard depending
     * on where the permission screen was opened.
     */
    private void continueFromPermissionScreen() {
        if (openedFromSettings) {
            finish();
            return;
        }

        Intent intent = new Intent(
                SmsPermissionActivity.this,
                DashboardActivity.class
        );

        startActivity(intent);
        finish();
    }
}