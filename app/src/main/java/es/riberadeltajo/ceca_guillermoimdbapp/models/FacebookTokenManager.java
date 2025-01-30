
package es.riberadeltajo.ceca_guillermoimdbapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class FacebookTokenManager {
    private static final String PREFS_FILENAME = "secure_prefs";
    private static final String ACCESS_TOKEN_KEY = "EAANvFfpUDF0BO9xZCzhj6vtkLJwUgdxFRZCBQNurXZCShFfAx5G1I0C8pgj2D8cZBSkEudQ5XGQ3HA3JJdNZBLDsNfUgqBWDAVLB4ZBOOMZA19ZBJILGzAXTl1mf3o2kaKxk80YGHzkzXa8tWHOY3kwtfkq6Rzpq1iBdV4K6PBYtqiEDCYW4t7vIM9s0geTT1pfHG3TcM4LMzvcMIqPULdtHIe87pwZDZD";
    private static FacebookTokenManager instance;
    private SharedPreferences sharedPreferences;

    private FacebookTokenManager(Context context) {
        try {
            // Genera o recupera la clave maestra para cifrar los SharedPreferences
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Inicializa EncryptedSharedPreferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_FILENAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Maneja la excepción adecuadamente en producción (ej. logging, alertas)
        }
    }

    /**
     * Obtiene la instancia única de FacebookTokenManager.
     *
     * @param context Contexto de la aplicación.
     * @return Instancia de FacebookTokenManager.
     */
    public static synchronized FacebookTokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new FacebookTokenManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Almacena el token de acceso de Facebook de manera segura.
     *
     * @param token Token de acceso de Facebook.
     */
    public void setAccessToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, token).apply();
        }
    }

    /**
     * Recupera el token de acceso de Facebook almacenado.
     *
     * @return Token de acceso de Facebook o null si no existe.
     */
    public String getAccessToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
        }
        return null;
    }

    /**
     * Elimina el token de acceso de Facebook almacenado.
     */
    public void clearAccessToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(ACCESS_TOKEN_KEY).apply();
        }
    }
}
