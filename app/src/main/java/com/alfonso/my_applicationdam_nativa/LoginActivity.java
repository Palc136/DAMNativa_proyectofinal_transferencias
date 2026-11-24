package com.alfonso.my_applicationdam_nativa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.alfonso.my_applicationdam_nativa.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

/**
 * LoginActivity autentica usuarios reales contra Firebase Auth y lee su rol
 * desde Firestore para saber qué pantalla de MainActivity abrir.
 */
public class LoginActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private ActivityLoginBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ¿Para qué sirve?: inicializar los servicios de Firebase.
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ¿Para qué sirve?: saltar el Login si ya hay sesión activa.
        // ¿Qué hace?: si FirebaseAuth ya tiene un usuario logueado, lee su rol y
        //             navega directo a MainActivity sin pedir credenciales.
        // ¿Cuál es su función en el código?: implementa "sesión persistente".
        if (auth.getCurrentUser() != null) {
            redirectByUserRole(auth.getCurrentUser().getUid());
            return;
        }

        // ¿Para qué sirve?: manejar el login real con correo y contraseña.
        // ¿Qué hace?: valida campos, llama a FirebaseAuth.signInWithEmailAndPassword y,
        //             si tiene éxito, lee el rol del usuario en Firestore.
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser() != null
                                ? authResult.getUser().getUid() : null;
                        if (uid == null) return;

                        // ¿Para qué sirve?: guardar credenciales cifradas para el próximo login biométrico.
                        // ¿Qué hace?: escribe email/password en EncryptedSharedPreferences.
                        // ¿Cuál es su función en el código?: permite que el botón de huella
                        //             recupere las credenciales y re-autentique sin pedir datos.
                        SecurePrefs.saveCredentials(this, email, password);

                        redirectByUserRole(uid);
                    })
                    .addOnFailureListener((Exception e) -> {
                        Toast.makeText(this,
                                "Credenciales incorrectas: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        // ¿Para qué sirve?: mantener el acceso biométrico.
        // (En la FASE B.3 lo conectaremos con credenciales reales).
        binding.btnBiometric.setOnClickListener(v -> showBiometricPrompt());

        // ¿Para qué sirve?: navegar al registro.
        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // ¿Para qué sirve?: navegar a la recuperación de contraseña.
        // (En la FASE B.2 lo haremos con FirebaseAuth.sendPasswordResetEmail).
        binding.btnRecover.setOnClickListener(v ->
                startActivity(new Intent(this, RecoverPasswordActivity.class)));
    }

    /**
     * Lee el rol del usuario en Firestore y navega a MainActivity con ese rol.
     * También verifica que el usuario esté activo.
     */
    private void redirectByUserRole(String uid) {

        // ¿Para qué sirve?: consultar el documento users/{uid} en Firestore.
        // ¿Qué hace?: obtiene el DocumentSnapshot y extrae "role" y "active".
        // ¿Cuál es su función en el código?: determina a qué pantalla ir y si la
        //             cuenta puede acceder.
        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Perfil de usuario no encontrado",
                                Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        return;
                    }

                    // ¿Para qué sirve?: verificar que la cuenta esté activa.
                    // ¿Qué hace?: lee el campo booleano "active"; si es null o false,
                    //             cierra sesión y muestra mensaje.
                    Boolean active = doc.getBoolean("active");
                    if (active == null || !active) {
                        Toast.makeText(this,
                                "Cuenta desactivada. Contacta al administrador.",
                                Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    // ¿Para qué sirve?: obtener el rol del usuario desde Firestore.
                    // ¿Qué hace?: lee el campo "role" (buyer | seller | admin).
                    String role = doc.getString("role");
                    if (role == null) role = "buyer";

                    // ¿Para qué sirve?: abrir la pantalla principal con el rol correspondiente.
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("ROLE", role);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener((Exception e) ->
                        Toast.makeText(this,
                                "Error al leer perfil: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Diálogo biométrico.
     * En la FASE B.3 se conectará con credenciales guardadas de forma segura.
     */
    /**
     * Muestra el diálogo biométrico y, si la huella es válida, re-autentica
     * al usuario con las credenciales guardadas cifradas.
     */
    private void showBiometricPrompt() {

        // ¿Para qué sirve?: verificar que existan credenciales guardadas antes de
        //                   mostrar el diálogo biométrico.
        // ¿Qué hace?: lee email/password de SecurePrefs; si son null (primer uso o
        //             tras logout), avisa al usuario que debe iniciar sesión manualmente.
        // ¿Cuál es su función en el código?: evita autenticar sin credenciales válidas.
        final String savedEmail = SecurePrefs.getEmail(this);
        final String savedPassword = SecurePrefs.getPassword(this);

        if (savedEmail == null || savedPassword == null) {
            Toast.makeText(this,
                    "Primero inicia sesión con correo y contraseña",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        // ¿Para qué sirve?: re-autenticar al usuario con las credenciales guardadas.
                        // ¿Qué hace?: llama a signInWithEmailAndPassword con los valores
                        //             cifrados; al tener éxito, redirige según rol.
                        // ¿Cuál es su función en el código?: completa el flujo biométrico
                        //             sin pedir credenciales manualmente.
                        auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                                .addOnSuccessListener(authResult -> {
                                    String uid = authResult.getUser() != null
                                            ? authResult.getUser().getUid() : null;
                                    if (uid != null) redirectByUserRole(uid);
                                })
                                .addOnFailureListener((Exception e) ->
                                        Toast.makeText(LoginActivity.this,
                                                "Error biométrico: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_subtitle))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}