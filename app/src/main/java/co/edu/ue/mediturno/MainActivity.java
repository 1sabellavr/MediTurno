package co.edu.ue.mediturno;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import co.edu.ue.mediturno.ui.auth.LoginActivity;
import co.edu.ue.mediturno.ui.citas.CitasFragment;
import co.edu.ue.mediturno.ui.inventario.InventarioFragment;
import co.edu.ue.mediturno.ui.usuarios.UsuariosFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String rolRecibido = getIntent().getStringExtra("rol");
        rol = rolRecibido != null ? rolRecibido : "";

        inicializarVistas();
        configurarToolbar();
        configurarMenuPorRol();
        configurarNavegacion(savedInstanceState);
    }

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void configurarToolbar() {
        toolbar.inflateMenu(R.menu.menu_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return false;
        });
    }

    private void configurarMenuPorRol() {
        // TODO-API: ajustar qué modulos ve cada rol según los roles que devuelva la API.
        // Regla provisional: ADMIN ve todo y cualquier otro rol solo ve Citas.
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);

        Menu menu = bottomNav.getMenu();
        menu.findItem(R.id.nav_usuarios).setVisible(esAdmin);
        menu.findItem(R.id.nav_inventario).setVisible(esAdmin);
        menu.findItem(R.id.nav_citas).setVisible(true);

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
        } else {
            toolbar.setTitle(R.string.nav_citas);
        }
    }

    private void cerrarSesion() {
        // TODO: borrar el token y el rol guardados cuando exista el manejo de sesion.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}