package com.alfonso.my_applicationdam_nativa.model;

/**
 * Product representa un producto del catálogo.
 * Incluye docId (ID del documento en Firestore) y la URL de la imagen subida a Cloudinary.
 */
public class Product {

    // ¿Para qué sirve?: identificar el documento en Firestore.
    // ¿Qué hace?: guarda el ID autogenerado por Firestore (String).
    // ¿Cuál es su función en el código?: con este ID se actualiza o elimina el producto.
    private String docId;

    private int id;
    private String name;
    private double price;
    private String description;
    private String category;

    // ¿Para qué sirve?: mostrar la imagen real del producto desde Cloudinary.
    // ¿Qué hace?: guarda la URL devuelta por MediaManager en onSuccess.
    // ¿Cuál es su función en el código?: Glide la carga en ImageView.
    private String imageUrl;

    // ¿Para qué sirve?: asociar el producto con su vendedor.
    // ¿Qué hace?: guarda el UID del vendedor autenticado.
    // ¿Cuál es su función en el código?: filtra los productos del vendedor logueado.
    private String vendedorId;

    // Constructor mínimo (compatibilidad con datos mock).
    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = "";
        this.category = "";
        this.imageUrl = "";
        this.vendedorId = "";
        this.docId = "";
    }

    // Constructor completo usado cuando se lee desde Firestore.
    public Product(String docId, int id, String name, double price,
                   String description, String category,
                   String imageUrl, String vendedorId) {
        this.docId = docId;
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.vendedorId = vendedorId;
    }

    public String getDocId() { return docId; }
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getVendedorId() { return vendedorId; }

    public void setDocId(String docId) { this.docId = docId; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
}