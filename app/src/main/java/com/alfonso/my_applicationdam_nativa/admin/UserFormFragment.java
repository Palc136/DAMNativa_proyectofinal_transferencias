package com.alfonso.my_applicationdam_nativa.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.SecurePrefs;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentUserFormBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * UserFormFragment crea un usuario real en Firebase Auth + Firestore desde el Admin.
 *
 * Workaround: createUserWithEmailAndPassword() cierra la sesión del admin y lo
 * reemplaza por el usuario recién creado. Para evitar que el admin pierda su sesión,
 * guardamos sus credenciales en SecurePrefs ANTES de crear el usuario y las usamos
 * para re-loguearlo DESPUÉS.
 */
public class UserFormFragment extends Fragment {

    private FragmentUserFormBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnSave.setOnClickListener(v -> crearUsuario());
    }

    /**
     * Flujo completo:
     *  1. Guardar credenciales del admin actual (email/password desde SecurePrefs).
     *  2. Crear el usuario nuevo en Firebase Auth.
     *  3. Guardar el perfil del usuario nuevo en Firestore.
     *  4. Re-loguear al admin con sus credenciales.
     */
    private void crearUsuario() {

        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        // ¿Para qué sirve?: obtener el rol elegido en los RadioButton.
        String role;
        if (binding.rbAdmin.isChecked()) {
            role = "admin";
        } else if (binding.rbSeller.isChecked()) {
            role = "seller";
        } else {
            role = "buyer";
        }

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // ¿Para qué sirve?: validar que el admin actual tenga credenciales guardadas.
        // ¿Qué hace?: lee email/password del admin desde SecurePrefs.
        // ¿Cuál es su función en el código?: sin esto, no podríamos re-loguear al admin
        //             después de crear el usuario nuevo.
        final String adminEmail = SecurePrefs.getEmail(requireContext());
        final String adminPassword = SecurePrefs.getPassword(requireContext());

        if (adminEmail == null || adminPassword == null) {
            Toast.makeText(requireContext(),
                    "Por seguridad, cierra sesión y vuelve a entrar antes de crear usuarios",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // ¿Para qué sirve?: leer la contraseña que el admin definió en el formulario.
        // ¿Qué hace?: obtiene el texto de etPassword y valida que tenga al menos 6 caracteres
        //(mínimo exigido por Firebase Auth).
        //¿Cuál es su función en el código?: reemplaza la contraseña fija
        //por la que el admin ingrese manualmente.
        final String defaultPassword = binding.etPassword.getText().toString().trim();

        if (defaultPassword.length() < 6) {
            Toast.makeText(requireContext(),
                    "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ¿Para qué sirve?: crear el usuario nuevo en Firebase Auth.
        // ¿Qué hace?: registra el correo con la contraseña temporal; devuelve el nuevo User.
        // ¿Cuál es su función en el código?: paso 2 del flujo.
        auth.createUserWithEmailAndPassword(email, defaultPassword)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser newUser = authResult.getUser();
                    if (newUser == null) {
                        Toast.makeText(requireContext(),
                                "Error al crear usuario", Toast.LENGTH_SHORT).show();
                        reloginAdmin(adminEmail, adminPassword);
                        return;
                    }

                    String newUid = newUser.getUid();

                    // ¿Para qué sirve?: construir el documento del nuevo usuario en Firestore.
                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("name", name);
                    userDoc.put("email", email);
                    userDoc.put("phone", "");
                    userDoc.put("role", role);
                    userDoc.put("active", true);
                    userDoc.put("createdAt", FieldValue.serverTimestamp());

                    // ¿Para qué sirve?: guardar el perfil en users/{newUid}.
                    db.collection("users").document(newUid).set(userDoc)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(),
                                        "Usuario creado exitosamente",
                                        Toast.LENGTH_SHORT).show();
                                // ¿Para qué sirve?: re-loguear al admin para que no pierda su sesión.
                                reloginAdmin(adminEmail, adminPassword);
                            })
                            .addOnFailureListener((Exception e) -> {
                                Toast.makeText(requireContext(),
                                        "Error al guardar perfil: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                reloginAdmin(adminEmail, adminPassword);
                            });
                })
                .addOnFailureListener((Exception e) -> {
                    Toast.makeText(requireContext(),
                            "Error al crear cuenta: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // En caso de fallo también re-logueamos al admin.
                    reloginAdmin(adminEmail, adminPassword);
                });
    }

    /**
     * Re-loguea al admin con sus credenciales guardadas para restaurar su sesión.
     */
    private void reloginAdmin(String adminEmail, String adminPassword) {

        // ¿Para qué sirve?: restaurar la sesión del admin tras crear el usuario.
        // ¿Qué hace?: signInWithEmailAndPassword con las credenciales del admin;
        //             al tener éxito, hace popBackStack() para volver al listado.
        // ¿Cuál es su función en el código?: cierra el flujo y restaura el estado original.
        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                .addOnSuccessListener(authResult -> {
                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener((Exception e) -> {
                    Toast.makeText(requireContext(),
                            "Error al restaurar sesión del admin: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}