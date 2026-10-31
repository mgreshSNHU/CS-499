package com.example.impetus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Manages the local SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "impetus.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GOAL_WEIGHT = "goal_weight";
    public static final String PHONE_NUMBER = "phone_number";

    // Weigh-ins table
    public static final String TABLE_WEIGH_INS = "weigh_ins";
    public static final String WEIGH_IN_ID = "id";
    public static final String WEIGH_IN_USER_ID = "user_id";
    public static final String WEIGHT = "weight";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String MOOD = "mood";
    public static final String NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database tables the first time the database is opened.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USERNAME + " TEXT NOT NULL UNIQUE, " +
                        PASSWORD + " TEXT NOT NULL, " +
                        GOAL_WEIGHT + " REAL, " +
                        PHONE_NUMBER + " TEXT" +
                        ")";

        String createWeighInsTable =
                "CREATE TABLE " + TABLE_WEIGH_INS + " (" +
                        WEIGH_IN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        WEIGH_IN_USER_ID + " INTEGER NOT NULL, " +
                        WEIGHT + " REAL NOT NULL, " +
                        DATE + " TEXT NOT NULL, " +
                        TIME + " TEXT NOT NULL, " +
                        MOOD + " INTEGER, " +
                        NOTES + " TEXT, " +
                        "FOREIGN KEY(" + WEIGH_IN_USER_ID + ") REFERENCES " +
                        TABLE_USERS + "(" + USER_ID + ")" +
                        ")";

        db.execSQL(createUsersTable);
        db.execSQL(createWeighInsTable);
    }

    /**
     * Recreates the database tables when the database version changes.
     */
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGH_INS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(db);
    }

    // User account methods

    /**
     * Creates a new local user account.
     *
     * @return the new database row ID, or -1 if the insert fails
     */
    public long createUser(
            String username,
            String password
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);

        return db.insert(
                TABLE_USERS,
                null,
                values
        );
    }

    /**
     * Validates a username and password against the users table.
     *
     * @return the matching user ID, or -1 if authentication fails
     */
    public long authenticateUser(
            String username,
            String password
    ) {
        SQLiteDatabase db = getReadableDatabase();

        String selection =
                USERNAME + " = ? AND " + PASSWORD + " = ?";

        String[] selectionArgs = {
                username,
                password
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(
                        cursor.getColumnIndexOrThrow(USER_ID)
                );
            }
        }

        return -1;
    }

    /**
     * Returns the username associated with the specified user ID.
     */
    public String getUsername(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USERNAME},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getString(
                        cursor.getColumnIndexOrThrow(USERNAME)
                );
            }
        }

        return "";
    }

    /**
     * Updates the user's username. A duplicate username violates
     * the table's UNIQUE constraint, so the method returns false if that occurs.
     */
    public boolean updateUsername(
            long userId,
            String username
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);

        try {
            int rowsUpdated = db.update(
                    TABLE_USERS,
                    values,
                    USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );

            return rowsUpdated > 0;

        } catch (SQLiteConstraintException exception) {
            return false;
        }
    }

    /**
     * Updates the user's target weight.
     */
    public boolean updateGoalWeight(
            long userId,
            double goalWeight
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GOAL_WEIGHT, goalWeight);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsUpdated > 0;
    }

    /**
     * Returns the user's target weight.
     *
     * @return the stored goal weight, or 0.0 if no goal has been set
     */
    public double getGoalWeight(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{GOAL_WEIGHT},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getDouble(0);
            }
        }

        return 0.0;
    }

    /**
     * Updates the phone number used for SMS goal notifications.
     */
    public void updatePhoneNumber(
            long userId,
            String phoneNumber
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PHONE_NUMBER, phoneNumber);

        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

    }

    /**
     * Returns the user's saved phone number.
     */
    public String getPhoneNumber(long userId) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{PHONE_NUMBER},
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
        }

        return "";
    }

    // Weigh-in CRUD methods

    /**
     * Creates a new weigh-in for the specified user.
     *
     * @return the new database row ID, or -1 if the insert fails
     */
    public long addWeighIn(
            long userId,
            double weight,
            String date,
            String time,
            int mood,
            String notes
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WEIGH_IN_USER_ID, userId);
        values.put(WEIGHT, weight);
        values.put(DATE, date);
        values.put(TIME, time);
        values.put(MOOD, mood);
        values.put(NOTES, notes);

        return db.insert(
                TABLE_WEIGH_INS,
                null,
                values
        );
    }

    /**
     * Retrieves all weigh-ins belonging to the specified user.
     */
    public ArrayList<WeighInEntry> getWeighInsForUser(
            long userId
    ) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<WeighInEntry> entries =
                new ArrayList<>();

        String selection =
                WEIGH_IN_USER_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(userId)
        };

        try (Cursor cursor = db.query(
                TABLE_WEIGH_INS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                WEIGH_IN_ID + " DESC"
        )) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(WEIGH_IN_ID)
                );

                double weight = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(WEIGHT)
                );

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(DATE)
                );

                String time = cursor.getString(
                        cursor.getColumnIndexOrThrow(TIME)
                );

                int mood = cursor.getInt(
                        cursor.getColumnIndexOrThrow(MOOD)
                );

                String notes = cursor.getString(
                        cursor.getColumnIndexOrThrow(NOTES)
                );

                entries.add(
                        new WeighInEntry(
                                id,
                                date,
                                time,
                                weight,
                                mood,
                                notes
                        )
                );
            }
        }

        return entries;
    }

    /**
     * Updates an existing weigh-in.
     */
    public boolean updateWeighIn(
            long weighInId,
            long userId,
            double weight,
            String date,
            String time,
            int mood,
            String notes
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WEIGHT, weight);
        values.put(DATE, date);
        values.put(TIME, time);
        values.put(MOOD, mood);
        values.put(NOTES, notes);

        int rowsUpdated = db.update(
                TABLE_WEIGH_INS,
                values,
                WEIGH_IN_ID + " = ? AND "
                        + WEIGH_IN_USER_ID + " = ?",
                new String[]{
                        String.valueOf(weighInId),
                        String.valueOf(userId)
                }
        );

        return rowsUpdated > 0;
    }

    /**
     * Deletes a weigh-in belonging to the specified user.
     */
    public boolean deleteWeighIn(
            long weighInId,
            long userId
    ) {
        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(
                TABLE_WEIGH_INS,
                WEIGH_IN_ID + " = ? AND "
                        + WEIGH_IN_USER_ID + " = ?",
                new String[]{
                        String.valueOf(weighInId),
                        String.valueOf(userId)
                }
        );

        return rowsDeleted > 0;
    }
}