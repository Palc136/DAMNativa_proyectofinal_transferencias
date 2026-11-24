package com.alfonso.my_applicationdam_nativa;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * SecurePrefs guarda y lee credenciales de forma cifrada usando EncryptedSharedPreferences.
 * Se usa para habilitar el login biométrico sin volver a pedir correo/contraseña.
 */
public class SecurePrefs {

    private static final String FILE_NAME = "pacoshop_secure_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    /**
     * Devuelve una instancia de EncryptedSharedPreferences con el Keystore de Android.
     */
    private static SharedPreferences getPrefs(Context context) throws Exception {

        // ¿Para qué sirve?: crear una clave maestra respaldada por el Keystore del dispositivo.
        // ¿Qué hace?: MasterKey.Builder configura el cifrado AES256_GCM.
        // ¿Cuál es su función en el código?: base criptográfica para cifrar las credenciales.
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // ¿Para qué sirve?: abrir el archivo de preferencias cifrado.
        // ¿Qué hace?: EncryptedSharedPreferences.create() cifra tanto claves como valores.
        return EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Guarda email y contraseña cifrados.
     */
    public static void saveCredentials(Context context, String email, String password) {
        try {
            getPrefs(context).edit()
                    .putString(KEY_EMAIL, email)
                    .putString(KEY_PASSWORD, password)
                    .apply();
        } catch (Exception ignored) { }
    }

    /**
     * Recupera el email guardado, o null si no existe.
     */
    public static String getEmail(Context context) {
        try {
            return getPrefs(context).getString(KEY_EMAIL, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Recupera la contraseña guardada, o null si no existe.
     */
    public static String getPassword(Context context) {
        try {
            return getPrefs(context).getString(KEY_PASSWORD, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Borra las credenciales (usar en logout).
     */
    public static void clear(Context context) {
        try {
            getPrefs(context).edit().clear().apply();
        } catch (Exception ignored) { }
    }
}
