package com.alfonso.my_applicationdam_nativa;

import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.alfonso.my_applicationdam_nativa.databinding.ActivityMainBinding;
import java.util.HashSet;
import java.util.Set;

/**
 * MainActivity es el contenedor principal de la aplicación.
 * Decide, según el rol recibido por Intent, qué gráfico de navegación
 * cargar y qué menú inferior mostrar, además de conectar el Drawer
 * y la Toolbar con el sistema de navegación.
 */
public class MainActivity extends AppCompatActivity {

    // ¿Para qué sirve?: mantener la referencia a las vistas de activity_main.xml.
    // ¿Qué hace?: guarda el binding generado por ViewBinding (drawerLayout, toolbar,
    //             navHostFragment, bottomNav, navView).
    // ¿Cuál es su función en el código?: acceso a las vistas desde cualquier método de la clase.
    @SuppressWarnings("FieldCanBeLocal")
    private ActivityMainBinding binding;
    // ¿Para qué sirve?: guardar la referencia al toggle del Drawer.
// ¿Qué hace?: expone el ActionBarDrawerToggle a toda la clase.
// ¿Cuál es su función en el código?: permite delegarle el click del ícono ☰
//             desde onOptionsItemSelected.
    private androidx.appcompat.app.ActionBarDrawerToggle drawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        // ¿Para qué sirve?: inicializar Cloudinary una sola vez por proceso.
// ¿Qué hace?: intenta MediaManager.init(); si ya estaba inicializado (por una
//             MainActivity previa en el mismo proceso), ignora la excepción.
// ¿Cuál es su función en el código?: evita el crash "MediaManager is already initialized"
//             cuando el usuario hace logout y vuelve a entrar, o cuando MainActivity
//             se recrea por rotación.
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "n0zjh9yh"); // ← reemplaza por tu Cloud Name real si aún no lo hiciste
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Ya estaba inicializado, no hacer nada.
        }


        // ¿Para qué sirve?: inflar el layout activity_main.xml y obtener el binding.
        // ¿Qué hace?: ActivityMainBinding.inflate construye la jerarquía de vistas
        //             y devuelve el objeto con todas las referencias por ID.
        // ¿Cuál es su función en el código?: inicializa la interfaz principal.

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ¿Para qué sirve?: determinar qué rol debe mostrarse.
        // ¿Qué hace?: lee el extra "ROLE" enviado desde LoginActivity o RegisterActivity;
        //             si llega null, asume "buyer".
        // ¿Cuál es su función en el código?: controla qué nav_graph y qué menú cargar.
        String role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "buyer";
        android.widget.Toast.makeText(this, "Rol recibido: " + role, android.widget.Toast.LENGTH_LONG).show();   // ← diagnóstico

        // ¿Para qué sirve?: obtener el NavHostFragment y su NavController.
        // ¿Qué hace?: busca por ID el FragmentContainerView configurado como NavHost
        //             y extrae el controlador de navegación.
        // ¿Cuál es su función en el código?: punto de acceso al sistema de navegación
        //             entre Fragments dentro de MainActivity.
        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHost.getNavController();

        // ¿Para qué sirve?: cargar el gráfico de navegación y el menú inferior adecuados.
        // ¿Qué hace?: según el rol, asigna el nav_graph_XXX y el bottom_nav_XXX
        // correspondientes al NavController y al BottomNavigationView.
        // ¿Cuál es su función en el código?: personaliza la experiencia de navegación
        // según el rol del usuario autenticado.
        // ¿Para qué sirve?: elegir el nav_graph, el bottom nav y los destinos top-level según el rol.
// ¿Qué hace?: carga el grafo correspondiente y construye el AppBarConfiguration
//             SOLO con los IDs del rol actual (no con los de los 3 roles).
// ¿Cuál es su función en el código?: al no mezclar IDs de distintos grafos, el
//             AppBarConfiguration reconoce correctamente el destino actual como
//             top-level y muestra el ícono de hamburguesa en la toolbar.
        Set<Integer> topLevelDestinations = new HashSet<>();

        if ("admin".equals(role)) {
            navController.setGraph(R.navigation.nav_graph_admin);
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_admin);
            topLevelDestinations.add(R.id.adminDashboardFragment);
            topLevelDestinations.add(R.id.usersFragment);
            topLevelDestinations.add(R.id.adminProductsFragment);
            topLevelDestinations.add(R.id.salesReportFragment);
        } else if ("seller".equals(role)) {
            navController.setGraph(R.navigation.nav_graph_seller);
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_seller);
            topLevelDestinations.add(R.id.sellerDashboardFragment);
            topLevelDestinations.add(R.id.sellerProductsFragment);
            topLevelDestinations.add(R.id.sellerOrdersFragment);
            topLevelDestinations.add(R.id.sellerProfileFragment);
        } else {
            navController.setGraph(R.navigation.nav_graph_buyer);
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_buyer);
            topLevelDestinations.add(R.id.homeFragment);
            topLevelDestinations.add(R.id.catalogFragment);
            topLevelDestinations.add(R.id.cartFragment);
            topLevelDestinations.add(R.id.profileFragment);
        }

// ¿Para qué sirve?: configurar los destinos "top-level" del Drawer.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                topLevelDestinations)
                .setOpenableLayout(binding.drawerLayout)
                .build();

// ¿Para qué sirve?: registrar la MaterialToolbar como ActionBar de la Activity.
// ¿Qué hace?: le indica a AppCompatActivity que la toolbar del layout es su barra oficial.
// ¿Cuál es su función en el código?: sin esto, setupActionBarWithNavController lanza
//             IllegalStateException y la app se cierra al abrir MainActivity.
        setSupportActionBar(binding.toolbar);


// ¿Para qué sirve?: asignar el toggle al campo de clase para usarlo en onOptionsItemSelected.
// ¿Qué hace?: crea el ActionBarDrawerToggle y lo guarda en this.drawerToggle.
// ¿Cuál es su función en el código?: al ser un campo, podemos llamar a su método
//             onOptionsItemSelected() desde el método de la Activity, lo que permite
//             que el click del ícono ☰ abra el Drawer.
        drawerToggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.app_name,
                R.string.app_name);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

// ¿Para qué sirve?: conectar el BottomNavigationView con el NavController.
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

// ¿Para qué sirve?: conectar la Toolbar con el NavController.
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // ¿Para qué sirve?: manejar el click en "Cerrar sesión" del Drawer.
        // ¿Qué hace?: registra un listener que, al pulsar nav_logout, ejecuta
        //FirebaseAuth.signOut() y navega al Login limpiando la pila.
        // ¿Cuál es su función en el código?: sin este listener, el ítem del Drawer
//             no hace nada porque nav_logout no es un destino del nav_graph.
        binding.navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                // ¿Para qué sirve?: borrar credenciales cifradas al cerrar sesión.
                // ¿Qué hace?: limpia email/password de EncryptedSharedPreferences.
                // ¿Cuál es su función en el código?: al borrar las credenciales, el login
                //             biométrico pedirá credenciales manuales tras el logout.
                SecurePrefs.clear(this);

                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            // Para cualquier otro ítem, delega el comportamiento por defecto de NavigationUI.
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });

        // ¿Para qué sirve?: desplazar el contenido debajo de la barra de estado.
        // ¿Qué hace?: aplica el inset superior (altura de la status bar) como padding
        // al DrawerLayout, evitando que la toolbar quede tapada por la hora.
        // ¿Cuál es su función en el código?: corrige el problema de edge-to-edge donde
        // el ícono ☰ queda detrás de la barra de estado y el sistema lo
        // interpreta como deslizar la barra de notificaciones.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout,
                (v, insets) -> {
                    androidx.core.graphics.Insets bars = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });
    }
    /**
     * Navega al destino cuyo ID coincida con un ítem del BottomNavigationView.
     * Se usa para que los botones de los Dashboards (vendedor / admin) muevan al usuario
     * a otra pestaña "como si hubiera pulsado el ícono del menú inferior", manteniendo
     * el BottomNav siempre sincronizado con el Fragment actual.
     */
    public void navigateToTab(int itemId) {

        // ¿Para qué sirve?: disparar la navegación a través del propio BottomNavigationView.
        // ¿Qué hace?: cambia el ítem seleccionado del BottomNav; internamente esto invoca al
        //             OnItemSelectedListener de setupWithNavController, que navega al Fragment
        //             correspondiente y actualiza el back stack de forma coherente.
        // ¿Cuál es su función en el código?: garantiza que el estado del BottomNav y el del
        //             NavController no se desincronicen, evitando el bug donde la pantalla no
        //             cambia aunque el ítem sí se marque.
        binding.bottomNav.setSelectedItemId(itemId);
    }
    /**
     * Maneja el click en los ítems del menú de la ActionBar (incluido el ícono ☰).
     */
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {

        // ¿Para qué sirve?: delegar el click del ícono ☰ al ActionBarDrawerToggle.
        // ¿Qué hace?: si el ítem pulsado es el "home" (android.R.id.home), el toggle
        //             abre o cierra el Drawer automáticamente.
        // ¿Cuál es su función en el código?: SIN ESTO, el ícono ☰ aparece pero no hace
        //             nada al pulsarlo porque setupActionBarWithNavController le quitó
        //             la acción original del toggle.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Para cualquier otro ítem, delega al comportamiento por defecto de NavigationUI.
        return super.onOptionsItemSelected(item);
    }
}