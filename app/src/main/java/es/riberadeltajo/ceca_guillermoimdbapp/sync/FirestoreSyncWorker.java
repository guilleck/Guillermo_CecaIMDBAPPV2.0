package es.riberadeltajo.ceca_guillermoimdbapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncWorker extends Worker {

    public FirestoreSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userId = getInputData().getString("userId");
        String name = getInputData().getString("name");
        String email = getInputData().getString("email");
        String lastLogin = getInputData().getString("lastLogin");
        String lastLogout = getInputData().getString("lastLogout");

        if (userId == null || userId.isEmpty()) {
            return Result.failure();
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> activityLog = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Object rawActivityLog = documentSnapshot.get("activity_log");

                        if (rawActivityLog instanceof List) {
                            activityLog = (List<Map<String, Object>>) rawActivityLog;
                        } else if (rawActivityLog instanceof Map) {
                            activityLog.add((Map<String, Object>) rawActivityLog);
                        }
                    }

                    Map<String, Object> activityEntry = new HashMap<>();
                    activityEntry.put("login_time", lastLogin != null ? lastLogin : "");
                    activityEntry.put("logout_time", lastLogout != null ? lastLogout : "");

                    activityLog.add(activityEntry);

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", userId);
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("activity_log", activityLog);

                    // 🔥 Subir los datos a Firestore sin sobrescribir `activity_log`
                    firestore.collection("users")
                            .document(userId)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Log.d("FIRESTORE_WORKER", "Historial actualizado correctamente"))
                            .addOnFailureListener(e -> Log.e("FIRESTORE_WORKER", "Error subiendo historial", e));
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE_WORKER", "Error obteniendo documento Firestore", e));

        return Result.success();
    }
}
