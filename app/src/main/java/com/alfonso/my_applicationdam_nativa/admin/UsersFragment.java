package com.alfonso.my_applicationdam_nativa.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alfonso.my_applicationdam_nativa.MainActivity;
import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.adapters.UserAdapter;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentUsersBinding;
import com.alfonso.my_applicationdam_nativa.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UsersFragment lista los usuarios reales desde Firestore y permite:
 * cambiar rol, activar/desactivar y eliminar (borrando el documento).
 */
public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private final List<User> users = new ArrayList<>();
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: configurar el RecyclerView con el adapter.
        adapter = new UserAdapter(users, this::showUserOptions);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        // ¿Para qué sirve?: navegar al formulario de usuario nuevo.
        binding.fabAddUser.setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).navigateToTab(R.id.userFormFragment);
            }
        });

        // ¿Para qué sirve?: escuchar la colección users en tiempo real.
        // ¿Qué hace?: cada vez que se cree, edite o elimine un usuario,
        //             la lista se actualiza automáticamente.
        FirebaseFirestore.getInstance()
                .collection("users")
                .addSnapshotListener((QuerySnapshot snapshots,
                                      com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(),
                                "Error al cargar usuarios: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    users.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String role = doc.getString("role") != null ? doc.getString("role") : "buyer";
                        Boolean active = doc.getBoolean("active");
                        User u = new User(
                                0,
                                doc.getString("name") != null ? doc.getString("name") : "—",
                                doc.getString("email") != null ? doc.getString("email") : "—",
                                // Convertir rol en minúsculas a texto capitalizado para el badge.
                                capitalizarRol(role),
                                active != null ? active : true
                        );
                        // ¿Para qué sirve?: guardar el UID real del documento en el campo id.
                        // ¿Qué hace?: usamos el setter setId o un campo adicional; como el modelo
                        //             original usa int, agregamos el UID al mapa mediante docId.
                        // ¿Cuál es su función en el código?: al eliminar o editar, se necesita el UID.
                        u.setId(doc.getId().hashCode()); // hash temporal solo como identificador local
                        // Guardamos también el docId real en el User via un setter auxiliar.
                        u.setDocId(doc.getId());
                        users.add(u);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Convierte "buyer" -> "Comprador", "seller" -> "Vendedor", "admin" -> "Administrador".
     */
    private String capitalizarRol(String role) {
        switch (role.toLowerCase()) {
            case "admin":  return "Administrador";
            case "seller": return "Vendedor";
            default:       return "Comprador";
        }
    }

    /**
     * Muestra las opciones de gestión para el usuario.
     */
    private void showUserOptions(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(user.getName())
                .setItems(new CharSequence[]{
                        "Cambiar rol",
                        user.isActive() ? "Desactivar" : "Activar",
                        "Eliminar usuario"
                }, (dialog, which) -> {
                    switch (which) {
                        case 0: cambiarRol(user); break;
                        case 1: toggleActivo(user); break;
                        case 2: confirmarEliminar(user); break;
                    }
                })
                .show();
    }

    /**
     * Cambia el rol del usuario en Firestore.
     */
    private void cambiarRol(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo rol")
                .setItems(new CharSequence[]{"Comprador", "Vendedor", "Administrador"},
                        (d, r) -> {
                            String[] roles = {"buyer", "seller", "admin"};
                            Map<String, Object> update = new HashMap<>();
                            update.put("role", roles[r]);
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.getDocId())
                                    .update(update)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(requireContext(),
                                                    "Rol actualizado", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener((Exception e) ->
                                            Toast.makeText(requireContext(),
                                                    "Error: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show());
                        })
                .show();
    }

    /**
     * Activa o desactiva el usuario.
     */
    private void toggleActivo(User user) {
        Map<String, Object> update = new HashMap<>();
        update.put("active", !user.isActive());
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getDocId())
                .update(update)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(requireContext(),
                                user.isActive() ? "Usuario desactivado" : "Usuario activado",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener((Exception e) ->
                        Toast.makeText(requireContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Confirma y elimina el documento del usuario de Firestore.
     */
    private void confirmarEliminar(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que quieres eliminar a \"" + user.getName() + "\"?\n\n" +
                        "Nota: la cuenta de Firebase Auth seguirá existiendo; solo se borra el perfil.")
                .setPositiveButton("Eliminar", (d, w) ->
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getDocId())
                                .delete()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(requireContext(),
                                                "Usuario eliminado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener((Exception e) ->
                                        Toast.makeText(requireContext(),
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}