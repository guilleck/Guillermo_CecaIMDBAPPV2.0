package es.riberadeltajo.ceca_guillermoimdbapp.sync;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.utils.FirestoreSyncWorker;

public class UserSync {

    private final FavoritesDatabaseHelper databaseHelper;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public UserSync(Context context) {
        this.databaseHelper = FavoritesDatabaseHelper.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void syncToFirestoreWithWorker(Context context) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_USERS, null);

        while (cursor.moveToNext()) {
            String userId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_EMAIL));
            String lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGIN));
            String lastLogout = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_LAST_LOGOUT));

            if (userId == null || userId.isEmpty()) {
                Log.e("FIRESTORE_SYNC", "Error: User ID es nulo o vacío, no se puede sincronizar.");
                continue;
            }

            Data inputData = new Data.Builder()
                    .putString("userId", userId)
                    .putString("name", name != null ? name : "Desconocido")
                    .putString("email", email != null ? email : "Sin email")
                    .putString("lastLogin", lastLogin != null ? lastLogin : "No registrado")
                    .putString("lastLogout", lastLogout != null ? lastLogout : "No registrado")
                    .build();

            OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(FirestoreSyncWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(context).enqueue(syncWorkRequest);
        }
        cursor.close();
    }
}
