package es.riberadeltajo.ceca_guillermoimdbapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class FacebookTokenManager {
    private static final String PREFS_FILENAME = "secure_prefs";
    private static final String ACCESS_TOKEN_KEY = "EAANvFfpUDF0BO2crsZALoLeZC0ciAVHjPijC6wY7V2k6D1J5mO1WmRlUMUTNd30yeQ766M5zYpqsvE7pzKnkVJc5eUltRM08QkWT3CjvxKHFwOY9qjFjYDDOs4ETkpKZCemQ4rswpE8jLtMCPaoDgpe6g5IKMo6ievrSeyJIM6grDwFgb9YMVmRoNUdGOmQVKq2S10UV5loC0KspNUtZCm5X7AZDZD";
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