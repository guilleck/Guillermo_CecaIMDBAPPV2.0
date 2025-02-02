
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

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;


public class AppLifecycleManager implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static final String PREF_NAME = "UserPrefs";
    private static final String PREF_IS_LOGGED_IN = "isLoggedIn";

    private boolean isInBackground = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private Handler logoutHandler = new Handler();
    private Runnable logoutRunnable = this::performLogout;
    private Context context;

    public AppLifecycleManager(Context context) {
        this.context = context;
    }

    @Override
    public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences++;
            if (activityReferences == 1 && isInBackground) {
                isInBackground = false;
                logoutHandler.removeCallbacks(logoutRunnable);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            if (activityReferences == 0) {
                isInBackground = true;
                performLogout();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isInBackground = true;
            performLogout();
        }
    }

    private synchronized void performLogout() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            registerUserLogout(currentUser);
            SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            preferences.edit().putBoolean(PREF_IS_LOGGED_IN, false).apply();
        }
    }


    private void registerUserLogout(FirebaseUser user) {
        if (user == null) return;
        String fechaLogout = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        FavoritesDatabaseHelper dbHelper = FavoritesDatabaseHelper.getInstance(context);
        dbHelper.updateLastLogout(user.getUid(), fechaLogout);
    }




    public void checkForPendingLogout() {

        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean wasLoggedIn = preferences.getBoolean(PREF_IS_LOGGED_IN, false);

        if (wasLoggedIn) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogout(currentUser);
            }

        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }
}
