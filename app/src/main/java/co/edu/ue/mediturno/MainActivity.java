package co.edu.ue.mediturno;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import co.edu.ue.mediturno.ui.auth.LoginActivity;
import co.edu.ue.mediturno.ui.citas.CitasFragment;
import co.edu.ue.mediturno.ui.inventario.InventarioFragment;
import co.edu.ue.mediturno.ui.tratamiento.TratamientoFragment;
import co.edu.ue.mediturno.ui.usuarios.UsuariosFragment;
import co.edu.ue.mediturno.util.NotificacionHelper;
import co.edu.ue.mediturno.util.PreferenciasHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Roles del sistema (deben escribirse igual que en la API).
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_MEDICO = "MEDICO";
    public static final String ROL_PACIENTE = "PACIENTE";

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private String rol;

    private final ActivityResultLauncher<String> permisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    concedido -> {
                        // Si el usuario no lo concede, la app sigue funcionando sin recordatorios.
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String rolRecibido = getIntent().getStringExtra("rol");
        rol = rolRecibido != null ? rolRecibido : "";

        NotificacionHelper.crearCanal(this);
        solicitarPermisoNotificaciones();

        inicializarVistas();
        configurarToolbar();
        configurarMenuPorRol();
        configurarNavegacion(savedInstanceState);
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void configurarToolbar() {
        toolbar.inflateMenu(R.menu.menu_toolbar);

        // SharedPreferences: estado guardado del interruptor de recordatorios de citas.
        MenuItem itemRecordatorios = toolbar.getMenu().findItem(R.id.action_recordatorios);
        itemRecordatorios.setChecked(PreferenciasHelper.obtenerNotificacionesActivas(this));

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_recordatorios) {
                boolean activar = !item.isChecked();
                item.setChecked(activar);
                PreferenciasHelper.guardarNotificacionesActivas(this, activar);
                Toast.makeText(this,
                        activar ? R.string.recordatorios_activados
                                : R.string.recordatorios_desactivados,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            if (item.getItemId() == R.id.action_cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            if (item.getItemId() == R.id.action_mapa) {
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Define qué módulos ve cada rol:
     * - ADMIN: Usuarios, Inventario y Citas.
     * - MEDICO: Inventario y Citas.
     * - PACIENTE (y cualquier otro rol): solo Citas.
     * Mi tratamiento (local en el teléfono) lo ven todos.
     */
    private void configurarMenuPorRol() {
        boolean esAdmin = ROL_ADMIN.equalsIgnoreCase(rol);
        boolean esMedico = ROL_MEDICO.equalsIgnoreCase(rol);

        Menu menu = bottomNav.getMenu();
        menu.findItem(R.id.nav_usuarios).setVisible(esAdmin);
        menu.findItem(R.id.nav_inventario).setVisible(esAdmin || esMedico);
        menu.findItem(R.id.nav_citas).setVisible(true);
        // Mi tratamiento es personal y local: lo ven todos los roles.
        menu.findItem(R.id.nav_tratamiento).setVisible(true);

        // Con un solo módulo visible no hace falta la barra inferior.
        if (contarItemsVisibles() <= 1) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private int contarItemsVisibles() {
        Menu menu = bottomNav.getMenu();
        int total = 0;
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).isVisible()) {
                total++;
            }
        }
        return total;
    }

    private int primerItemVisible() {
        Menu menu = bottomNav.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) {
                return item.getItemId();
            }
        }
        return R.id.nav_citas;
    }

    private void configurarNavegacion(Bundle savedInstanceState) {
        bottomNav.setOnItemSelectedListener(item -> {
            mostrarFragment(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(primerItemVisible());
        } else {
            actualizarTitulo(bottomNav.getSelectedItemId());
        }
    }

    private void mostrarFragment(int idItem) {
        Fragment fragment;
        if (idItem == R.id.nav_usuarios) {
            fragment = new UsuariosFragment();
        } else if (idItem == R.id.nav_inventario) {
            fragment = new InventarioFragment();
        } else if (idItem == R.id.nav_tratamiento) {
            fragment = new TratamientoFragment();
        } else {
            fragment = new CitasFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragment, fragment)
                .commit();

        actualizarTitulo(idItem);
    }

    private void actualizarTitulo(int idItem) {
        if (idItem == R.id.nav_usuarios) {
            toolbar.setTitle(R.string.nav_usuarios);
        } else if (idItem == R.id.nav_inventario) {
            toolbar.setTitle(R.string.nav_inventario);
        } else if (idItem == R.id.nav_tratamiento) {
            toolbar.setTitle(R.string.nav_tratamiento);
        } else {
            toolbar.setTitle(R.string.nav_citas);
        }
    }

    private void cerrarSesion() {
        // Cierra la sesión de Firebase; si no, el Login lo reenvía a la pantalla principal.
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}