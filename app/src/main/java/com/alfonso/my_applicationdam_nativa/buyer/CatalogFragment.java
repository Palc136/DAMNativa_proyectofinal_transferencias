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
import com.alfonso.my_applicationdam_nativa.databinding.FragmentCatalogBinding;
import com.alfonso.my_applicationdam_nativa.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * CatalogFragment muestra el catálogo completo leído desde Firestore.
 * El SearchView filtra en tiempo real sobre los productos cargados.
 */
public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;

    // ¿Para qué sirve?: guardar la lista completa en memoria.
    // ¿Qué hace?: se mantiene como fuente para aplicar filtros de búsqueda.
    // ¿Cuál es su función en el código?: el SearchView genera sublistas sin volver a Firestore.
    private final List<Product> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: configurar RecyclerView antes de llegar los datos.
        binding.rvCatalog.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ¿Para qué sirve?: cargar productos desde Firestore en tiempo real.
        // ¿Qué hace?: addSnapshotListener escucha cambios en la colección "products"
        //             y actualiza la lista cuando alguien crea/edita/elimina.
        FirebaseFirestore.getInstance()
                .collection("products")
                .addSnapshotListener((QuerySnapshot snapshots,
                                      com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(),
                                "Error al cargar catálogo: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null) return;

                    // ¿Para qué sirve?: convertir los documentos en objetos Product.
                    allProducts.clear();
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
                        allProducts.add(p);
                    }
                    actualizarLista(allProducts);
                });

        // ¿Para qué sirve?: escuchar el texto del SearchView.
        binding.searchView.setOnQueryTextListener(
                new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterProducts(query);
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProducts(newText);
                        return true;
                    }
                });
    }

    /**
     * Asigna la lista al adapter con listener de click hacia el detalle.
     */
    private void actualizarLista(List<Product> lista) {
        binding.rvCatalog.setAdapter(
                new ProductAdapter(lista, this::navigateToDetail));
    }

    /**
     * Filtra productos por nombre (case-insensitive).
     */
    private void filterProducts(String query) {
        List<Product> filtrados = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                filtrados.add(p);
            }
        }
        actualizarLista(filtrados);
    }

    /**
     * Navega al detalle enviando el docId del producto.
     */
    private void navigateToDetail(Product product) {

        // ¿Para qué sirve?: enviar el docId al detalle para que consulte Firestore.
        // ¿Qué hace?: solo se pasa el ID; el detalle consulta los datos frescos.
        // ¿Cuál es su función en el código?: reduce el payload del Bundle y garantiza
        //             que el detalle muestra datos actualizados.
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