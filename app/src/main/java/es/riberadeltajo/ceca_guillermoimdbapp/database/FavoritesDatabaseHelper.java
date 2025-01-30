package es.riberadeltajo.ceca_guillermoimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.AccessControlContext;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 3;

    public static final String COLUMN_RATING = "rating";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_USER_ID_FAVORITES = "user_id";

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    public static final String COLUMN_LAST_LOGOUT = "last_logout";

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_RELEASE_DATE + " TEXT, " +
                COLUMN_RATING + " TEXT, " +
                COLUMN_POSTER_PATH + " TEXT, " +
                COLUMN_USER_ID + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USER_ID_FAVORITES + "))";
        db.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT , " +
                COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_LAST_LOGIN + " TEXT, " +
                COLUMN_LAST_LOGOUT + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);
    }

    public void insertOrUpdateUser(String userId, String name, String email, String lastLogin, String lastLogout) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USER_ID + " = ?", new String[]{userId});

        boolean existe = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_LAST_LOGIN, lastLogin);
        values.put(COLUMN_LAST_LOGOUT, lastLogout);

        if (!existe) {
            long result = db.insert(TABLE_USERS, null, values);
        } else {
            int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        }

        db.close();
    }


    public void updateLastLogin(String userId, String lastLogin) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, lastLogin);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }


    public void updateLastLogout(String userId, String lastLogout) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGOUT, lastLogout);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }
}
