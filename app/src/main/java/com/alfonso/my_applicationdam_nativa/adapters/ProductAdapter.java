package com.alfonso.my_applicationdam_nativa.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.databinding.ItemProductBinding;
import com.alfonso.my_applicationdam_nativa.model.Product;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * ProductAdapter conecta una lista de Product con un RecyclerView.
 * Si el producto tiene imageUrl, la carga con Glide; si no, muestra fondo gris.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final OnProductClick onProductClick;
    private final OnAddToCartClick onAddToCartClick;

    public interface OnProductClick {
        void onClick(Product product);
    }

    public interface OnAddToCartClick {
        void onAdd(Product product);
    }

    // Constructor sin listeners (compatibilidad).
    public ProductAdapter(List<Product> products) {
        this(products, null, null);
    }

    // Constructor con listener de click (catálogo).
    public ProductAdapter(List<Product> products, OnProductClick onProductClick) {
        this(products, onProductClick, null);
    }

    // Constructor completo.
    public ProductAdapter(List<Product> products,
                          OnProductClick onProductClick,
                          OnAddToCartClick onAddToCartClick) {
        this.products = products;
        this.onProductClick = onProductClick;
        this.onAddToCartClick = onAddToCartClick;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;
        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.tvName.setText(product.getName());
        holder.binding.tvCategory.setText(product.getCategory());
        holder.binding.tvPrice.setText("$" + String.format("%,.0f", product.getPrice()));

        // ¿Para qué sirve?: cargar la imagen real del producto desde Cloudinary.
        // ¿Qué hace?: si imageUrl no está vacía, Glide la descarga y la muestra;
        //             si está vacía, aplica el color de fondo gris claro.
        // ¿Cuál es su función en el código?: reemplaza el placeholder vacío
        //             por la foto real del producto.
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.color.surface_light)
                    .error(R.color.surface_light)
                    .into(holder.binding.imgProduct);
        } else {
            holder.binding.imgProduct.setImageResource(R.color.surface_light);
        }

        // ¿Para qué sirve?: manejar el click del botón "Agregar al carrito".
        holder.binding.btnAdd.setOnClickListener(v -> {
            if (onAddToCartClick != null) {
                onAddToCartClick.onAdd(product);
            } else {
                Toast.makeText(v.getContext(),
                        product.getName() + " agregado al carrito",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ¿Para qué sirve?: manejar el click sobre la tarjeta.
        holder.binding.getRoot().setOnClickListener(v -> {
            if (onProductClick != null) onProductClick.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}