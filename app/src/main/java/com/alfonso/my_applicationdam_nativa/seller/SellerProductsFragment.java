package com.alfonso.my_applicationdam_nativa.seller;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.adapters.ProductAdapter;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentSellerProductsBinding;
import com.alfonso.my_applicationdam_nativa.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * SellerProductsFragment lista los productos del vendedor autenticado desde Firestore,
 * permite crear uno nuevo con el FAB, editarlo o eliminarlo con long-press.
 */
public class SellerProductsFragment extends Fragment {

    private FragmentSellerProductsBinding binding;
    private final List<Product> products = new ArrayList<>();
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: configurar el RecyclerView con el adapter.
        adapter = new ProductAdapter(products);
        binding.rvSellerProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSellerProducts.setAdapter(adapter);

        // ¿Para qué sirve?: detectar long-press para editar/eliminar.
        final GestureDetector detector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(@NonNull MotionEvent e) {
                        View child = binding.rvSellerProducts.findChildViewUnder(e.getX(), e.getY());
                        if (child != null) {
                            int pos = binding.rvSellerProducts.getChildAdapterPosition(child);
                            if (pos >= 0 && pos < products.size()) {
                                showProductOptions(products.get(pos));
                            }
                        }
                    }
                });
        binding.rvSellerProducts.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                         @NonNull MotionEvent e) {
                        return detector.onTouchEvent(e);
                    }
                });

        // ¿Para qué sirve?: navegar al formulario de producto nuevo.
        binding.fabAdd.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.productFormFragment);
        });

        // ¿Para qué sirve?: cargar la lista de productos del vendedor desde Firestore.
        cargarMisProductos();
    }

    /**
     * Lee de Firestore los productos cuyo vendedorId coincide con el usuario autenticado.
     */
    private void cargarMisProductos() {

        // ¿Para qué sirve?: obtener el UID del vendedor logueado.
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ¿Para qué sirve?: escuchar cambios en tiempo real en la colección products.
        // ¿Qué hace?: addSnapshotListener se dispara cada vez que cambia la colección
        //             y devuelve la lista actualizada.
        // ¿Cuál es su función en el código?: al crear, editar o eliminar un producto,
        //             la lista se actualiza automáticamente sin recargar la pantalla.
        FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("vendedorId", uid)
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(),
                                "Error al cargar: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) return;

                    // ¿Para qué sirve?: convertir cada documento en un objeto Product.
                    products.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Product p = new Product(
                                doc.getId(),
                                0,
                                doc.getString("nombre"),
                                doc.getDouble("precio") != null ? doc.getDouble("precio") : 0,
                                doc.getString("descripcion") != null ? doc.getString("descripcion") : "",
                                doc.getString("categoria") != null ? doc.getString("categoria") : "",
                                doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "",
                                doc.getString("vendedorId") != null ? doc.getString("vendedorId") : ""
                        );
                        products.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Muestra el diálogo de Editar / Eliminar para el producto.
     */
    private void showProductOptions(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle(product.getName())
                .setItems(new CharSequence[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        // ¿Para qué sirve?: navegar al formulario con el docId para editarlo.
                        Bundle args = new Bundle();
                        args.putString("docId", product.getDocId());
                        NavController nav = Navigation.findNavController(requireView());
                        nav.navigate(R.id.productFormFragment, args);
                    } else {
                        eliminarProducto(product);
                    }
                })
                .show();
    }

    /**
     * Elimina el producto de Firestore.
     */
    private void eliminarProducto(Product product) {

        // ¿Para qué sirve?: confirmar la eliminación antes de borrar.
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que quieres eliminar \"" + product.getName() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {

                    // ¿Para qué sirve?: borrar el documento products/{docId} de Firestore.
                    // ¿Qué hace?: document(docId).delete(); el SnapshotListener
                    //             actualizará la lista automáticamente.
                    // ¿Cuál es su función en el código?: elimina el producto del catálogo.
                    FirebaseFirestore.getInstance()
                            .collection("products")
                            .document(product.getDocId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(requireContext(),
                                            "Producto eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener((Exception e) ->
                                    Toast.makeText(requireContext(),
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}