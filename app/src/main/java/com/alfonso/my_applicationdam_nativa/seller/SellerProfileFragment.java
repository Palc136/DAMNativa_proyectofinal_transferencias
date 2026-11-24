package com.alfonso.my_applicationdam_nativa.seller;

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
import com.alfonso.my_applicationdam_nativa.databinding.FragmentSellerProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SellerProfileFragment muestra los datos reales del vendedor autenticado
 * y permite cerrar sesión de Firebase.
 */
public class SellerProfileFragment extends Fragment {

    private FragmentSellerProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cargarDatosUsuario();

        binding.btnEditShop.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Editar tienda (próximamente)", Toast.LENGTH_SHORT).show());
        // ¿Para qué sirve?: cerrar sesión de Firebase + volver al Login.
        // ¿Qué hace?: llama a FirebaseAuth.signOut() (limpia el usuario cacheado)
//         y lanza LoginActivity limpiando la pila de Activities.
//         ¿Cuál es su función en el código?: sin signOut(), el Login detectaría sesión
//         activa y re-entraría automáticamente.
        binding.btnLogout.setOnClickListener(v -> {
            // ¿Para qué sirve?: borrar credenciales cifradas al cerrar sesión.
            SecurePrefs.clear(requireContext());

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void cargarDatosUsuario() {

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    if (name != null) binding.tvShopName.setText(name);
                    if (email != null) binding.tvNit.setText(email);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}