package es.riberadeltajo.ceca_guillermoimdbapp.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.sync.UserSync;

public class AppLifecycleManager extends Application implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static final String PREF_NAME = "UserPrefs";
    private static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    private boolean isInBackground = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private Context context;

    private final Handler logoutHandler = new Handler();
    private final Runnable logoutRunnable = this::performLogout;

    public AppLifecycleManager(Context context) {
        this.context = context;
    }

    public AppLifecycleManager() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean wasLoggedIn = preferences.getBoolean(PREF_IS_LOGGED_IN, false);

        if (wasLoggedIn) {
            performLogout();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) { }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences++;
            if (activityReferences == 1 && isInBackground) {
                // Volvemos a primer plano: cancelamos logout
                isInBackground = false;
                logoutHandler.removeCallbacks(logoutRunnable);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) { }

    @Override
    public void onActivityPaused(Activity activity) { }

    @Override
    public void onActivityStopped(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            // Si ya no hay Activities visibles, consideramos logout
            if (activityReferences == 0) {
                isInBackground = true;
                performLogout();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            isInBackground = true;
            performLogout();
            new UserSync(this).syncToFirestoreWithWorker(this);
        }
    }

    private synchronized void performLogout() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            registerUserLogout(currentUser);

            SharedPreferences preferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            preferences.edit().putBoolean(PREF_IS_LOGGED_IN, false).apply();

            new UserSync(this).syncToFirestoreWithWorker(this);
        }
    }

    private void registerUserLogout(FirebaseUser user) {
        if (user == null) return;

        String fechaLogout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        FavoritesDatabaseHelper dbHelper = FavoritesDatabaseHelper.getInstance(this);
        dbHelper.updateLastLogout(user.getUid(), fechaLogout);

        new UserSync(this).syncToFirestoreWithWorker(this);

    }


    public void checkForPendingLogout() {
        SharedPreferences preferences =
                this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean wasLoggedIn = preferences.getBoolean(PREF_IS_LOGGED_IN, false);

        // Si había sesión previa y se reabrió la app sin hacer login,
        // podríamos forzar el registro de logout en local
        if (wasLoggedIn) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogout(currentUser);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        performLogout();
    }
}
