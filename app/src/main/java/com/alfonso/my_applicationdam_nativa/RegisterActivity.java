package com.alfonso.my_applicationdam_nativa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alfonso.my_applicationdam_nativa.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * RegisterActivity crea usuarios reales en Firebase Auth + un documento en Firestore
 * con su nombre, correo, teléfono, rol y estado.
 */
public class RegisterActivity extends AppCompatActivity {

    // ¿Para qué sirve?: mantener la referencia al binding del layout.
    @SuppressWarnings("FieldCanBeLocal")
    private ActivityRegisterBinding binding;

    // ¿Para qué sirve?: instancia de Firebase Auth para crear usuarios.
    // ¿Qué hace?: expone createUserWithEmailAndPassword, signIn, signOut, etc.
    // ¿Cuál es su función en el código?: es el servicio de autenticación.
    private FirebaseAuth auth;

    // ¿Para qué sirve?: instancia de Firestore para guardar el documento del usuario.
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ¿Para qué sirve?: inicializar los servicios de Firebase.
        // ¿Qué hace?: obtiene las instancias singleton de Auth y Firestore.
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ¿Para qué sirve?: manejar el registro real del usuario.
        // ¿Qué hace?: valida los campos, crea el usuario en Firebase Auth y, si tiene éxito,
        //             crea el documento en Firestore con el rol elegido.
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            // ¿Para qué sirve?: determinar el rol elegido en los RadioButton.
            // ¿Qué hace?: guarda "buyer" o "seller" según cuál esté marcado.
            // ¿Cuál es su función en el código?: será el valor del campo "role" en Firestore.
            String role = binding.rbBuyer.isChecked() ? "buyer" : "seller";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // ¿Para qué sirve?: crear el usuario en Firebase Auth.
            // ¿Qué hace?: registra el correo y contraseña; devuelve un AuthResult con el uid.
            // ¿Cuál es su función en el código?: paso 1 del registro (auth).
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        // ¿Para qué sirve?: obtener el UID generado por Firebase Auth.
                        // ¿Qué hace?: lee authResult.getUser().getUid().
                        // ¿Cuál es su función en el código?: es la clave del documento en Firestore.
                        String uid = authResult.getUser() != null
                                ? authResult.getUser().getUid() : null;

                        if (uid == null) {
                            Toast.makeText(this, "Error al crear cuenta",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ¿Para qué sirve?: armar el documento del usuario en Firestore.
                        // ¿Qué hace?: construye un Map con name, email, phone, role,
                        //             active=true y createdAt=timestamp del servidor.
                        // ¿Cuál es su función en el código?: paso 2 del registro (persistencia).
                        Map<String, Object> userDoc = new HashMap<>();
                        userDoc.put("name", name);
                        userDoc.put("email", email);
                        userDoc.put("phone", phone);
                        userDoc.put("role", role);
                        userDoc.put("active", true);
                        userDoc.put("createdAt", FieldValue.serverTimestamp());

                        // ¿Para qué sirve?: guardar el documento en users/{uid}.
                        // ¿Qué hace?: escribe el Map en Firestore y maneja éxito / error.
                        // ¿Cuál es su función en el código?: cierra el flujo de registro.
                        db.collection("users").document(uid)
                                .set(userDoc)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registro exitoso",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener((Exception e) -> {
                                    Toast.makeText(this,
                                            "Error al guardar perfil: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener((Exception e) -> {
                        Toast.makeText(this,
                                "Error al registrar: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
}