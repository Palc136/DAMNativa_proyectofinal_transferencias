package com.alfonso.my_applicationdam_nativa.buyer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentProductDetailBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ProductDetailFragment carga el producto desde Firestore usando el docId
 * recibido desde el catálogo o el home.
 */
public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: obtener el docId del producto a mostrar.
        // ¿Qué hace?: lee el Bundle de argumentos enviado por el catálogo o el home.
        Bundle args = getArguments();
        String docId = (args != null) ? args.getString("docId") : null;

        if (docId == null) {
            Toast.makeText(requireContext(),
                    "Producto no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // ¿Para qué sirve?: consultar el documento products/{docId} en Firestore.
        // ¿Qué hace?: llena los TextViews y carga la imagen con Glide al recibir los datos.
        // ¿Cuál es su función en el código?: muestra el detalle siempre actualizado.
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(docId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(),
                                "Producto eliminado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    binding.tvName.setText(doc.getString("nombre"));
                    binding.tvCategory.setText(doc.getString("categoria"));
                    Double price = doc.getDouble("precio");
                    binding.tvPrice.setText("$" + String.format("%,.0f",
                            price != null ? price : 0.0));
                    binding.tvDescription.setText(doc.getString("descripcion"));

                    // ¿Para qué sirve?: cargar la imagen real del producto.
                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.color.surface_light)
                                .into(binding.imgProduct);
                    }
                })
                .addOnFailureListener((Exception e) ->
                        Toast.makeText(requireContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());

        // ¿Para qué sirve?: manejar el click de "Agregar al carrito" (mock).
        binding.btnAddToCart.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Producto agregado al carrito",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}