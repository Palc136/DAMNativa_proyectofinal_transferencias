package com.alfonso.my_applicationdam_nativa;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alfonso.my_applicationdam_nativa.databinding.ActivityRecoverPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * RecoverPasswordActivity envía un correo real de restablecimiento de contraseña
 * usando FirebaseAuth.sendPasswordResetEmail().
 */
public class RecoverPasswordActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private ActivityRecoverPasswordBinding binding;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecoverPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ¿Para qué sirve?: inicializar el servicio de autenticación.
        auth = FirebaseAuth.getInstance();

        // ¿Para qué sirve?: enviar el correo de restablecimiento de contraseña.
        // ¿Qué hace?: valida el correo y llama a sendPasswordResetEmail();
        //             Firebase envía un enlace al email del usuario.
        // ¿Cuál es su función en el código?: cierra el flujo de recuperación real.
        binding.btnSend.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ¿Para qué sirve?: disparar el envío del correo.
            // ¿Qué hace?: si el correo existe, Firebase envía un enlace;
            //             si no existe, devuelve error.
            // ¿Cuál es su función en el código?: paso único del flujo de recuperación.
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Enlace enviado a " + email + ". Revisa tu bandeja.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener((Exception e) -> {
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}
