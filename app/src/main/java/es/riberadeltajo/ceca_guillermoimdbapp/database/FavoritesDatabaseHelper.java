package es.riberadeltajo.ceca_guillermoimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.firestore.auth.User;

import java.security.AccessControlContext;

public class FavoritesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 6;

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
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IMAGE = "image";

    private static FavoritesDatabaseHelper instance;

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized FavoritesDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesDatabaseHelper(context.getApplicationContext());
        }
        return instance;
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
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_IMAGE + " TEXT, " +
                COLUMN_LAST_LOGIN + " TEXT, " +
                COLUMN_LAST_LOGOUT + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

    }

    public synchronized void insertOrUpdateUser(String userId, String name, String email, String lastLogin, String lastLogout,
                                   String phone, String address, String photoUrl) {
        SQLiteDatabase db = getWritableDatabase();

        // Comprobamos si el usuario ya existe
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
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_IMAGE, photoUrl);

        if (!existe) {
            db.insert(TABLE_USERS, null, values);
        } else {
            db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        }

        db.close();
    }



    public void updateUserProfile(String userId, String name, String address, String phone, String image) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ADDRESS, address);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }

    public void updatePhotoUrl(String userId, String photoUrl) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("photo_url", photoUrl);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }


    public void updateLastLogin(String userId, String lastLogin) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, lastLogin);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }

    public synchronized void updateLastLogout(String userId, String lastLogout) {
        SQLiteDatabase db = getWritableDatabase();
        try {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGOUT, lastLogout);
        int rows = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
        } finally {
            db.close();
        }
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHONE + " TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_IMAGE + " TEXT");
        }

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();
    }

   

}