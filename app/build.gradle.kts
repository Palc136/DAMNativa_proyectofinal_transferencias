plugins {
    alias(libs.plugins.android.application)



    id("com.google.gms.google-services")
}

android {

    // ¿Para qué sirve?: habilitar la generación automática de clases Binding para cada layout XML.
// ¿Qué hace?: le dice al plugin de Android que genere una clase Java por cada archivo de layout
//             (por ejemplo, ActivityLoginBinding para activity_login.xml).
// ¿Cuál es su función en el código?: permite acceder a las vistas del layout desde Java
//             sin usar findViewById y sin riesgo de NullPointerException por IDs mal escritos.
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.alfonso.my_applicationdam_nativa"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.alfonso.my_applicationdam_nativa"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // ¿Para qué sirve?: habilitar la carga de imágenes desde URLs (Cloudinary) en los ImageView.
    // ¿Qué hace?: descarga e integra el SDK de Glide 4.16.0.
    // ¿Cuál es su función en el código?: permite usar Glide.with(...).load(imageUrl).into(imageView).
    implementation("com.github.bumptech.glide:glide:4.16.0")

// (Opcional pero recomendado para Java) Procesador de anotaciones de Glide.
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ¿Para qué sirve?: almacenar credenciales de forma cifrada en el dispositivo.
    // ¿Qué hace?: agrega EncryptedSharedPreferences, que cifra las claves con el Keystore de Android.
    // ¿Cuál es su función en el código?: guardar email/password seguros para el login biométrico.
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ¿Para qué sirve?: Integrar el SDK oficial de Cloudinary para Android.
    // ¿Qué hace?: Añade las clases MediaManager, UploadRequest y callbacks para subir archivos.
    // ¿Cuál es su función en el código?: Permite subir imágenes desde la app a la nube de Cloudinary.
    implementation("com.cloudinary:cloudinary-android:3.1.3")

    // Import the Firebase BoM

    implementation(platform("com.google.firebase:firebase-bom:35.2.0"))


    // TODO: Add the dependencies for Firebase products you want to use

    // When using the BoM, don't specify versions in Firebase dependencies

    // https://firebase.google.com/docs/android/setup#available-libraries

    // ¿Para qué sirve?: Agregar las librerías de Firebase al proyecto.
// ¿Qué hace?: importa Firebase Auth (login), Firestore (base de datos),
//             Storage (imágenes) y el BoM que alinea las versiones.
// ¿Cuál es su función en el código?: habilita las clases FirebaseAuth,
//             FirebaseFirestore y FirebaseStorage en Java.
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")


    // ¿Para qué sirve?: dependencias base del proyecto (ya venían en la plantilla).
    // ¿Qué hace?: aportan AppCompat, Material, Activity y ConstraintLayout.
    // ¿Cuál es su función en el código?: soporte de Activities, temas Material 3 y layouts.
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ¿Para qué sirve?: Navigation Component (Fragments y rutas).
    // ¿Qué hace?: permite navegar entre pantallas usando nav_graph y BottomNav/Drawer.
    // ¿Cuál es su función en el código?: sistema de navegación entre Fragments.
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // ¿Para qué sirve?: autenticación biométrica.
    // ¿Qué hace?: expone BiometricPrompt para login con huella.
    // ¿Cuál es su función en el código?: ofrece login biométrico en LoginActivity.
    implementation(libs.androidx.biometric)

    // ¿Para qué sirve?: FragmentContainerView y utilidades de Fragments.
    // ¿Qué hace?: base para NavHostFragment y Fragments con ViewBinding.
    // ¿Cuál es su función en el código?: infraestructura de Fragments del proyecto.
    implementation(libs.androidx.fragment)

    // ¿Para qué sirve?: RecyclerView para listados.
    // ¿Qué hace?: dibuja listas eficientes de productos, usuarios, pedidos, etc.
    // ¿Cuál es su función en el código?: componente de listas usado en todas las pantallas de catálogo.
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

}