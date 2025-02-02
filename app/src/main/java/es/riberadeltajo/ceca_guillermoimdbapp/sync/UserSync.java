package es.riberadeltajo.ceca_guillermoimdbapp.sync;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;

public class UserSync {

    private final FavoritesDatabaseHelper databaseHelper;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public UserSync(Context context) {
        this.databaseHelper = FavoritesDatabaseHelper.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public synchronized void syncToFirestore() {
        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = databaseHelper.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_USERS, null);

                while (cursor.moveToNext()) {
                    String userId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_USER_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_EMAIL));
                    String lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGIN));
                    String lastLogout = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGOUT));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_IMAGE));

                    Map<String, String> activityEntry = new HashMap<>();
                    if (lastLogin != null) activityEntry.put("login_time", lastLogin);
                    if (lastLogout != null) activityEntry.put("logout_time", lastLogout);

                    uploadUserToFirestore(userId, name, email, image, activityEntry);
                }
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
        }).start();
    }





    private String getCurrentLastLogoutFromFirestore(String userId) {
        final String[] lastLogout = {null};

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, String>> activityLog = (List<Map<String, String>>) documentSnapshot.get("activity_log");
                        if (activityLog != null && !activityLog.isEmpty()) {
                            for (Map<String, String> activity : activityLog) {
                                if (activity.containsKey("logout_time")) {
                                    lastLogout[0] = activity.get("logout_time");
                                }
                            }
                        }
                    }
                });

        return lastLogout[0];
    }

    private void uploadUserToFirestore(String userId, String name, String email, String image, Map<String, String> activityEntry) {
        if (userId == null || email == null) {
            return;
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, String>> activityLog = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        activityLog = (List<Map<String, String>>) documentSnapshot.get("activity_log");
                        if (activityLog == null) {
                            activityLog = new ArrayList<>();
                        }
                    }

                    if (activityEntry.containsKey("login_time") && activityEntry.get("login_time") != null) {
                        activityEntry.put("login_time", activityEntry.get("login_time"));
                    }

                    if (activityEntry.containsKey("logout_time") && activityEntry.get("logout_time") != null) {
                        activityEntry.put("logout_time", activityEntry.get("logout_time"));
                    }

                    if (!activityEntry.isEmpty()) {
                        activityLog.add(activityEntry);
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", userId);
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("image", image != null ? image : "");
                    userData.put("activity_log", activityLog);

                    firestore.collection("users")
                            .document(userId)
                            .set(userData, SetOptions.merge());
                });
    }

    public synchronized void syncFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> new Thread(() -> {
                    try (SQLiteDatabase db = databaseHelper.getWritableDatabase()) {  // ✅ Se cierra automáticamente
                        db.beginTransaction();

                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String image = documentSnapshot.getString("image");

                            List<Map<String, String>> activityLog = (List<Map<String, String>>) documentSnapshot.get("activity_log");
                            String lastLogin = null, lastLogout = null;

                            if (activityLog != null && !activityLog.isEmpty()) {
                                for (Map<String, String> activity : activityLog) {
                                    if (activity.containsKey("login_time")) {
                                        lastLogin = activity.get("login_time");
                                    }
                                    if (activity.containsKey("logout_time")) {
                                        lastLogout = activity.get("logout_time");
                                    }
                                }
                            }

                            if (lastLogout == null || lastLogout.isEmpty()) {
                                lastLogout = getCurrentLastLogoutFromSQLite(userId);
                            }

                            databaseHelper.insertOrUpdateUser(userId, name, email, lastLogin, lastLogout, null, null, image);
                        }

                        db.setTransactionSuccessful();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start());
    }


    private String getCurrentLastLogoutFromSQLite(String userId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String lastLogout = null;
        Cursor cursor = db.rawQuery("SELECT " + FavoritesDatabaseHelper.COLUMN_LAST_LOGOUT + " FROM " + FavoritesDatabaseHelper.TABLE_USERS + " WHERE " + FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId});
        if (cursor != null && cursor.moveToFirst()) {
            lastLogout = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return lastLogout;
    }
}
