package com.alfonso.my_applicationdam_nativa.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alfonso.my_applicationdam_nativa.SecurePrefs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alfonso.my_applicationdam_nativa.LoginActivity;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentBuyerProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * BuyerProfileFragment muestra los datos reales del comprador autenticado
 * y permite cerrar sesión cerrando también la sesión de Firebase.
 */
public class BuyerProfileFragment extends Fragment {

    private FragmentBuyerProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBuyerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: cargar los datos del usuario autenticado.
        // ¿Qué hace?: lee users/{uid} en Firestore y actualiza tvName y tvEmail.
        cargarDatosUsuario();

        // ¿Para qué sirve?: manejar el click de "Editar perfil" (mock).
        binding.btnEditProfile.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Editar perfil (próximamente)", Toast.LENGTH_SHORT).show());

        // ¿Para qué sirve?: manejar el click de "Mis direcciones" (mock).
        binding.btnMyAddresses.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Mis direcciones (próximamente)", Toast.LENGTH_SHORT).show());

        // ¿Para qué sirve?: cerrar sesión de Firebase + volver al Login.
        // ¿Qué hace?: llama a FirebaseAuth.signOut() (limpia el usuario cacheado)
        //             y luego lanza LoginActivity limpiando la pila de Activities.
        // ¿Cuál es su función en el código?: sin signOut(), el Login detectaría
        //             sesión activa y re-entraría automáticamente.
        binding.btnLogout.setOnClickListener(v -> {
            // ¿Para qué sirve?: borrar las credenciales cifradas al cerrar sesión.
            // ¿Qué hace?: limpia email/password de EncryptedSharedPreferences.
            // ¿Cuál es su función en el código?: al borrar las credenciales, el próximo
            //             intento de login biométrico pedirá correo/contraseña manualmente
            //             en lugar de re-autenticar con datos viejos.
            SecurePrefs.clear(requireContext());

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /**
     * Lee el documento users/{uid} en Firestore y actualiza la UI.
     */
    private void cargarDatosUsuario() {

        // ¿Para qué sirve?: obtener el UID del usuario autenticado actualmente.
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) return;

        // ¿Para qué sirve?: consultar el documento del usuario en Firestore.
        // ¿Qué hace?: obtiene name y email del documento users/{uid}.
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    if (name != null) binding.tvName.setText(name);
                    if (email != null) binding.tvEmail.setText(email);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}