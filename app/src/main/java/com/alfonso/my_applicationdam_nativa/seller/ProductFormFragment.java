package com.alfonso.my_applicationdam_nativa.seller;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alfonso.my_applicationdam_nativa.R;
import com.alfonso.my_applicationdam_nativa.databinding.FragmentProductFormBinding;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ProductFormFragment permite crear un producto del vendedor.
 * Flujo completo: elegir imagen -> subir a Cloudinary -> guardar en Firestore.
 */
public class ProductFormFragment extends Fragment {

    private FragmentProductFormBinding binding;

    // ¿Para qué sirve?: Guardar la URI de la imagen elegida por el usuario.
    // ¿Qué hace?: Almacena el Uri devuelto por el selector de imágenes del sistema.
    // ¿Cuál es su función en el código?: Se usa como fuente para la subida a Cloudinary.
    private Uri selectedImageUri = null;

    // ¿Para qué sirve?: Abrir el selector de imágenes del sistema.
    // ¿Qué hace?: Registra un launcher que, al terminar, recibe la Uri elegida y la
    //             asigna a selectedImageUri, además de previsualizarla en imgPreview.
    // ¿Cuál es su función en el código?: Es el punto de entrada para elegir la imagen.
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.imgPreview.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ¿Para qué sirve?: detectar si el formulario está en modo edición.
        // ¿Qué hace?: lee el Bundle de argumentos; si trae "docId", precarga los campos.
        // ¿Cuál es su función en el código?: permite reutilizar el mismo formulario
        //             para crear y para editar.
        Bundle args = getArguments();
        if (args != null && args.getString("docId") != null) {
            String docId = args.getString("docId");
            precargarProducto(docId);
        }

        binding.btnSelectImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // ¿Para qué sirve?: Permitir al usuario elegir una imagen de la galería.
        // ¿Qué hace?: Al pulsar el botón, lanza el launcher con tipo "image/*".
        // ¿Cuál es su función en el código?: Dispara el flujo de selección de imagen.
        binding.btnSelectImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        // ¿Para qué sirve?: Manejar el guardado completo del producto.
        // ¿Qué hace?: Valida los campos, verifica que haya imagen elegida y, si todo
        //             está bien, inicia la subida a Cloudinary.
        // ¿Cuál es su función en el código?: Es el botón que dispara todo el flujo
        //             (subida + guardado en Firestore).
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String price = binding.etPrice.getText().toString().trim();
            String description = binding.etDescription.getText().toString().trim();
            String stock = binding.etStock.getText().toString().trim();
            String category = binding.etCategory.getText().toString().trim();

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(),
                        "Selecciona una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // ¿Para qué sirve?: Iniciar la subida de la imagen y luego guardar en Firestore.
            subirImagenACloudinary(selectedImageUri, name, price, description, stock, category);
        });
    }

    /**
     * Precarga los campos del formulario con los datos de Firestore.
     */
    private void precargarProducto(String docId) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    binding.etName.setText(doc.getString("nombre"));
                    binding.etDescription.setText(doc.getString("descripcion"));
                    binding.etCategory.setText(doc.getString("categoria"));

                    Double price = doc.getDouble("precio");
                    if (price != null) binding.etPrice.setText(String.valueOf(price.intValue()));

                    Long stock = doc.getLong("stock");
                    if (stock != null) binding.etStock.setText(String.valueOf(stock));

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Cargar la imagen existente en el preview con Glide.
                        com.bumptech.glide.Glide.with(requireContext())
                                .load(imageUrl)
                                .into(binding.imgPreview);
                    }
                });
    }

    /**
     * Sube la imagen seleccionada a Cloudinary (Paso 5).
     * Al terminar con éxito, llama a guardarProductoEnFirestore (Paso 6).
     */
    private void subirImagenACloudinary(Uri imageUri,
                                        String name, String price,
                                        String description, String stock,
                                        String category) {

        // ¿Para qué sirve?: Enviar la imagen a Cloudinary usando el preset "unsigned".
        // ¿Qué hace?: MediaManager.get().upload(imageUri) prepara la subida;
        //             .unsigned("pacoshop_products") indica el preset sin firma;
        //             .callback(...) define qué hacer al terminar;
        //             .dispatch() arranca la subida en segundo plano.
        // ¿Cuál es su función en el código?: Es el Paso 5: transformar un archivo local
        //             en una URL pública que podamos guardar en Firestore.
        String requestId = MediaManager.get().upload(imageUri)
                .unsigned("pacoshop_products") // <-- REEMPLAZA por tu preset si usaste otro nombre
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        // ¿Para qué sirve?: Notificar que la subida comenzó.
                        // ¿Qué hace?: Muestra un Toast informativo.
                        Toast.makeText(requireContext(),
                                "Subiendo imagen...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // ¿Para qué sirve?: Opcional: actualizar una barra de progreso.
                        // ¿Qué hace?: No hace nada aquí, pero puedes añadir un ProgressBar.
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        // ¿Para qué sirve?: Obtener la URL final de la imagen ya subida.
                        // ¿Qué hace?: El mapa resultData contiene "secure_url", que es la
                        //             URL HTTPS lista para usar en Firestore.
                        // ¿Cuál es su función en el código?: Es el dato clave que conecta
                        //             Cloudinary (imagen) con Firestore (producto).
                        String imageUrl = (String) resultData.get("secure_url");

                        // ¿Para qué sirve?: Guardar el producto con la URL en Firestore (Paso 6).
                        guardarProductoEnFirestore(imageUrl, name, price,
                                description, stock, category);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        // ¿Para qué sirve?: Notificar al usuario si falla la subida.
                        // ¿Qué hace?: Muestra un Toast con la descripción del error.
                        Toast.makeText(requireContext(),
                                "Error al subir imagen: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // ¿Para qué sirve?: Avisar que el SDK reintentará la subida.
                        // ¿Qué hace?: No hace nada; el SDK lo maneja automáticamente.
                    }
                })
                .dispatch();
    }

    /**
     * Guarda el producto en Firestore (Paso 6).
     */
    private void guardarProductoEnFirestore(String imageUrl,
                                            String name, String price,
                                            String description, String stock,
                                            String category) {

        // ¿Para qué sirve?: Construir el documento del producto con todos sus campos.
        // ¿Qué hace?: Crea un Map con nombre, precio, descripción, stock, categoría,
        //             la URL de la imagen y el UID del vendedor autenticado.
        // ¿Cuál es su función en el código?: Es el objeto que se guarda en Firestore.
        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", name);
        producto.put("precio", Double.parseDouble(price));
        producto.put("descripcion", description);
        producto.put("stock", stock.isEmpty() ? 0 : Integer.parseInt(stock));
        producto.put("categoria", category);
        producto.put("imageUrl", imageUrl);
        producto.put("vendedorId",
                FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "desconocido");

        // ¿Para qué sirve?: decidir entre crear o actualizar un producto.
// ¿Qué hace?: si el formulario tiene un docId (edición), hace set() sobre el
//             documento existente; si no, hace add() para crear uno nuevo.
// ¿Cuál es su función en el código?: reutiliza el mismo formulario para ambos flujos.
        Bundle args = getArguments();
        String docIdEdit = (args != null) ? args.getString("docId") : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (docIdEdit != null) {
            // Modo edición.
            db.collection("products").document(docIdEdit).set(producto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    })
                    .addOnFailureListener((Exception e) ->
                            Toast.makeText(requireContext(),
                                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            // Modo creación.
            db.collection("products").add(producto)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    })
                    .addOnFailureListener((Exception e) ->
                            Toast.makeText(requireContext(),
                                    "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}