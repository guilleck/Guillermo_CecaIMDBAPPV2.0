package es.riberadeltajo.ceca_guillermoimdbapp.utils;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.riberadeltajo.ceca_guillermoimdbapp.MainActivity;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;


public class AppLifecycleManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    private boolean isInBackground = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private boolean isAppClosed = false;

    private Handler logoutHandler = new Handler();
    private Runnable logoutRunnable = this::performLogout;

    private Context context;

    public AppLifecycleManager(Context context) {
        this.context = context;
    }

    public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {
    }


    public void onActivityStarted(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences++;
            if (activityReferences == 1 && isInBackground) {
                // La aplicación vuelve a primer plano
                isInBackground = false;
                logoutHandler.removeCallbacks(logoutRunnable);
            }
        }
    }


    public void onActivityResumed(Activity activity) {
    }


    public void onActivityPaused(Activity activity) {
    }


    public void onActivityStopped(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            if (activityReferences == 0) {
                // La aplicación ha pasado a segundo plano
                isInBackground = true;
                performLogout(); // Registrar logout sin cerrar sesión
            }
        }
    }


    public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {
    }


    public void onActivityDestroyed(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // La aplicación ha pasado a segundo plano
            isInBackground = true;
            performLogout(); // Registrar logout sin cerrar sesión
        }
    }

    private void performLogout() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            registerUserLogout(currentUser);
            Log.d("AppLifecycleManager", "Logout registrado automáticamente al pasar a segundo plano.");
        }
    }

    private void registerUserLogout(FirebaseUser user) {
        // Crear la fecha de logout
        String fechaLogout = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());

        // Actualizar en la base de datos
        FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(context);
        dbHelper.updateLastLogout(user.getUid(), fechaLogout);

        Log.d("AppLifecycleManager", "Logout registrado para el usuario: " + user.getUid());
    }


    public void checkForPendingLogout() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean wasLoggedIn = preferences.getBoolean(PREF_IS_LOGGED_IN, false);

        if (wasLoggedIn) {
            // Solo registrar el logout sin cerrar sesión
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogout(currentUser);
            }

            Log.d("AppLifecycleManager", "Logout pendiente registrado al reiniciar la app.");
        }
    }


    public void onConfigurationChanged(@NonNull Configuration newConfig) {

    }


    public void onLowMemory() {

    }
}
