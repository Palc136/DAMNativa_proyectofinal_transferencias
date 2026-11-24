package com.alfonso.my_applicationdam_nativa.buyer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.adapters.ProductAdapter;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentBuyerHomeBinding;
import com.alfonso.my_applicationdam_nativa.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment muestra hasta 6 productos destacados leídos desde Firestore.
 */
public class HomeFragment extends Fragment {

    private FragmentBuyerHomeBinding binding;
    private final List<Product> destacados = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBuyerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ¿Para qué sirve?: traer los primeros productos de Firestore como "destacados".
        // ¿Qué hace?: limita a 6 documentos y escucha cambios en tiempo real.
        // ¿Cuál es su función en el código?: alimenta el listado de la pantalla de inicio.
        FirebaseFirestore.getInstance()
                .collection("products")
                .limit(6)
                .addSnapshotListener((QuerySnapshot snapshots,
                                      com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    destacados.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Product p = new Product(
                                doc.getId(),
                                0,
                                doc.getString("nombre") != null ? doc.getString("nombre") : "",
                                doc.getDouble("precio") != null ? doc.getDouble("precio") : 0,
                                doc.getString("descripcion") != null ? doc.getString("descripcion") : "",
                                doc.getString("categoria") != null ? doc.getString("categoria") : "",
                                doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "",
                                doc.getString("vendedorId") != null ? doc.getString("vendedorId") : ""
                        );
                        destacados.add(p);
                    }

                    binding.rvProducts.setAdapter(
                            new ProductAdapter(destacados, this::navigateToDetail));
                });
    }

    /**
     * Navega al detalle del producto con el docId.
     */
    private void navigateToDetail(Product product) {
        Bundle args = new Bundle();
        args.putString("docId", product.getDocId());
        NavController nav = Navigation.findNavController(requireView());
        nav.navigate(R.id.productDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}