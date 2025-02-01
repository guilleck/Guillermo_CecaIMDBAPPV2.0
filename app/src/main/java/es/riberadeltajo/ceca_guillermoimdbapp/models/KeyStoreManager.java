package es.riberadeltajo.ceca_guillermoimdbapp.models;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class KeyStoreManager {
    private static final String TAG = "KeystoreManager";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "MyAppKeyAlias";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    private SecretKey secretKey;

    public KeyStoreManager(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            // Verificar si la clave ya existe
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateKey();
            }

            // Obtener la clave del almacén de claves
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            if (secretKeyEntry != null) {
                secretKey = secretKeyEntry.getSecretKey();
            }

            if (secretKey == null) {
                Log.e(TAG, "Error: SecretKey es null después de la inicialización.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando KeyStoreManager", e);
        }
    }

    private void generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
            Log.d(TAG, "Clave AES generada y almacenada en Keystore.");

            // Volver a obtener la clave generada
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            if (secretKeyEntry != null) {
                secretKey = secretKeyEntry.getSecretKey();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generando clave AES", e);
        }
    }

    public String encrypt(String plainText) {
        try {
            if (secretKey == null) {
                Log.e(TAG, "Error: SecretKey es null al intentar encriptar.");
                return null;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Concatenar IV y cipherText
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            byte[] cipherMessage = byteBuffer.array();

            // Codificar en Base64
            return Base64.encodeToString(cipherMessage, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encriptando datos", e);
            return null;
        }
    }

    public String decrypt(String cipherText) {
        try {
            if (secretKey == null) {
                Log.e(TAG, "Error: SecretKey es null al intentar desencriptar.");
                return null;
            }

            byte[] cipherMessage = Base64.decode(cipherText, Base64.DEFAULT);

            // Extraer IV y cipherText
            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);
            byte[] actualCipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(actualCipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decryptedBytes = cipher.doFinal(actualCipherText);

            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error desencriptando datos", e);
            return null;
        }
    }
}
