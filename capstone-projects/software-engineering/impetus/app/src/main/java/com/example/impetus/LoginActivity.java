package com.example.impetus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Handles user authentication and account creation.
 */
public class LoginActivity extends AppCompatActivity {

    // Application preferences
    private static final String PREFS_NAME = "ImpetusPrefs";
    private static final String KEY_USER_ID = "user_id";

    // Login controls
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;

    // Database
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        setupWindowInsets();
        initializeViews();
        setupLoginButton();
        setupCreateAccountButton();
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
     * Initializes the controls used by the login screen.
     */
    private void initializeViews() {
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
    }

    /**
     * Configures authentication for existing users.
     */
    private void setupLoginButton() {
        findViewById(R.id.buttonLogin)
                .setOnClickListener(view -> loginUser());
    }

    /**
     * Validates entered credentials and authenticates the user.
     */
    private void loginUser() {
        String username =
                getUsernameInput();

        String password =
                getPasswordInput();

        if (hasInvalidRequiredFields(
                username,
                password
        )) {
            return;
        }

        long userId =
                databaseHelper.authenticateUser(
                        username,
                        password
                );

        if (userId != -1) {
            saveLoggedInUser(userId);

            Intent intent = new Intent(
                    LoginActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(
                    LoginActivity.this,
                    R.string.invalid_credentials,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Configures creation of new Impetus accounts.
     */
    private void setupCreateAccountButton() {
        findViewById(R.id.buttonCreateAccount)
                .setOnClickListener(view -> createAccount());
    }

    /**
     * Validates and creates a new user account.
     */
    private void createAccount() {
        String username =
                getUsernameInput();

        String password =
                getPasswordInput();

        if (hasInvalidRequiredFields(
                username,
                password
        )) {
            return;
        }

        if (password.length() < 6) {
            editPassword.setError(
                    getString(R.string.password_too_short)
            );

            editPassword.requestFocus();
            return;
        }

        long userId =
                databaseHelper.createUser(
                        username,
                        password
                );

        if (userId != -1) {
            saveLoggedInUser(userId);

            Toast.makeText(
                    LoginActivity.this,
                    R.string.account_created,
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    LoginActivity.this,
                    SmsPermissionActivity.class
            );

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(
                    LoginActivity.this,
                    R.string.username_exists,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Returns the trimmed username entered by the user.
     */
    private String getUsernameInput() {
        return editUsername.getText() == null
                ? ""
                : editUsername.getText()
                .toString()
                .trim();
    }

    /**
     * Returns the password entered by the user.
     */
    private String getPasswordInput() {
        return editPassword.getText() == null
                ? ""
                : editPassword.getText()
                .toString();
    }

    /**
     * Checks whether either required credential field is missing.
     */
    private boolean hasInvalidRequiredFields(
            String username,
            String password
    ) {
        if (username.isEmpty()) {
            editUsername.setError(
                    getString(R.string.username_required)
            );

            editUsername.requestFocus();
            return true;
        }

        if (password.isEmpty()) {
            editPassword.setError(
                    getString(R.string.password_required)
            );

            editPassword.requestFocus();
            return true;
        }

        return false;
    }

    /**
     * Stores the authenticated user ID for use across the application.
     */
    private void saveLoggedInUser(long userId) {
        getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        )
                .edit()
                .putLong(KEY_USER_ID, userId)
                .apply();
    }
}