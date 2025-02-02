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

    public void syncToFirestore() {
        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;

            try {
                db = databaseHelper.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_USERS, null);

                while (cursor != null && cursor.moveToNext()) {
                    String userId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_USER_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_EMAIL));
                    String lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGIN));
                    String lastLogout = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGOUT));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_IMAGE));

                    Map<String, String> activityEntry = new HashMap<>();
                    activityEntry.put("login_time", lastLogin != null ? lastLogin : "");
                    activityEntry.put("logout_time", lastLogout != null ? lastLogout : ""); // Captura el logout_time

                    uploadUserToFirestore(userId, name, email, image, activityEntry);
                }
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }).start();
    }



    private void uploadUserToFirestore(String userId, String name, String email, String image, Map<String, String> activityEntry) {
        if (userId == null || email == null) {
            return; // Si faltan datos críticos, no hacemos nada
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, String>> activityLog = (List<Map<String, String>>) documentSnapshot.get("activity_log");
                    if (activityLog == null) {
                        activityLog = new ArrayList<>();
                    }

                    // Asegurar que el logout_time se incluye correctamente
                    if (activityEntry.get("login_time") != null || activityEntry.get("logout_time") != null) {
                        activityLog.add(activityEntry);
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", userId);
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("image", image);
                    userData.put("activity_log", activityLog); // Subir el historial completo

                    firestore.collection("users")
                            .document(userId)
                            .set(userData, SetOptions.merge());
                });
    }


    public void syncFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        String userId = user.getUid();
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> new Thread(() -> {
                    SQLiteDatabase db = null;

                    try {
                        // Intento de obtener la base de datos con reintento en caso de bloqueo
                        for (int i = 0; i < 5; i++) {
                            try {
                                db = databaseHelper.getWritableDatabase();
                                break;
                            } catch (Exception e) {
                                Thread.sleep(100);
                            }
                        }

                        if (db == null) {
                            return;
                        }

                        db.beginTransaction();

                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String image = documentSnapshot.getString("image");

                            // Obtener último registro del historial de actividad
                            List<Map<String, String>> activityLog = (List<Map<String, String>>) documentSnapshot.get("activity_log");
                            String lastLogin = null;
                            String lastLogout = null;

                            if (activityLog != null && !activityLog.isEmpty()) {
                                Map<String, String> lastActivity = activityLog.get(activityLog.size() - 1);
                                lastLogin = lastActivity.get("login_time");
                                lastLogout = lastActivity.get("logout_time");
                            }

                            // Insertar o actualizar en SQLite
                            databaseHelper.insertOrUpdateUser(userId, name, email, lastLogin, lastLogout, null, null, image);
                        }

                        db.setTransactionSuccessful();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (db != null && db.isOpen()) {
                            db.endTransaction();
                            db.close();
                        }
                    }
                }).start());
    }

}
