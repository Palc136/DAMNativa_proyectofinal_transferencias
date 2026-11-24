package com.alfonso.my_applicationdam_nativa.admin;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alfonso.my_applicationdam_nativa.adapters.ProductAdapter;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentAdminProductsBinding;
import com.alfonso.my_applicationdam_nativa.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminProductsFragment supervisa todos los productos del sistema desde Firestore.
 * Permite desactivar (eliminar) un producto con long-press.
 */
public class AdminProductsFragment extends Fragment {

    private FragmentAdminProductsBinding binding;
    private final List<Product> products = new ArrayList<>();
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ProductAdapter(products);
        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminProducts.setAdapter(adapter);

        // ¿Para qué sirve?: detectar long-press para "desactivar" un producto.
        final GestureDetector detector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(@NonNull MotionEvent e) {
                        View child = binding.rvAdminProducts.findChildViewUnder(e.getX(), e.getY());
                        if (child != null) {
                            int pos = binding.rvAdminProducts.getChildAdapterPosition(child);
                            if (pos >= 0 && pos < products.size()) {
                                showProductOptions(products.get(pos));
                            }
                        }
                    }
                });
        binding.rvAdminProducts.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                         @NonNull MotionEvent e) {
                        return detector.onTouchEvent(e);
                    }
                });

        // ¿Para qué sirve?: cargar TODOS los productos de Firestore en tiempo real.
        FirebaseFirestore.getInstance()
                .collection("products")
                .addSnapshotListener((QuerySnapshot snapshots,
                                      com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    products.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        products.add(new Product(
                                doc.getId(),
                                0,
                                doc.getString("nombre") != null ? doc.getString("nombre") : "",
                                doc.getDouble("precio") != null ? doc.getDouble("precio") : 0,
                                doc.getString("descripcion") != null ? doc.getString("descripcion") : "",
                                doc.getString("categoria") != null ? doc.getString("categoria") : "",
                                doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "",
                                doc.getString("vendedorId") != null ? doc.getString("vendedorId") : ""
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Muestra las opciones de supervisión para el producto.
     */
    private void showProductOptions(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle(product.getName())
                .setItems(new CharSequence[]{"Desactivar producto"}, (d, w) -> {
                    FirebaseFirestore.getInstance()
                            .collection("products")
                            .document(product.getDocId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(requireContext(),
                                            "Producto desactivado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener((Exception e) ->
                                    Toast.makeText(requireContext(),
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}