package co.edu.ue.mediturno.ui.usuarios;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.adapter.UsuarioAdapter;
import co.edu.ue.mediturno.model.Usuario;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class UsuariosFragment extends Fragment implements UsuarioAdapter.OnUsuarioListener {

    private final List<Usuario> usuarios = new ArrayList<>();
    private UsuarioAdapter adapter;
    private TextView tvVacio;
    private ActivityResultLauncher<Intent> formLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        procesarResultado(result.getData());
                    }
                });

        cargarDatosDePrueba();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_usuarios, container, false);

        RecyclerView rvUsuarios = vista.findViewById(R.id.rvUsuarios);
        tvVacio = vista.findViewById(R.id.tvVacio);
        ExtendedFloatingActionButton fabNuevo = vista.findViewById(R.id.fabNuevoUsuario);

        adapter = new UsuarioAdapter(usuarios, this);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsuarios.setAdapter(adapter);

        fabNuevo.setOnClickListener(v ->
                formLauncher.launch(new Intent(requireContext(), UsuarioFormActivity.class)));

        actualizarVacio();
        return vista;
    }

    private void cargarDatosDePrueba() {
        // TODO-API: GET /usuarios
        // Recibe: lista de Usuario (id, nombre, documento, teléfono, correo y rol).
        // Reemplazar estos datos de prueba por la respuesta de la API.
        usuarios.add(new Usuario(1, "Laura Gómez", "1012345678", "3001234567",
                "laura.gomez@correo.com", "ADMIN"));
        usuarios.add(new Usuario(2, "Carlos Pérez", "80123456", "3109876543",
                "carlos.perez@correo.com", "MEDICO"));
        usuarios.add(new Usuario(3, "María Rodríguez", "52987654", "3205551122",
                "maria.rodriguez@correo.com", "PACIENTE"));
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(usuarios.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void procesarResultado(Intent data) {
        int id = data.getIntExtra(UsuarioFormActivity.EXTRA_ID, 0);
        String nombre = data.getStringExtra(UsuarioFormActivity.EXTRA_NOMBRE);
        String documento = data.getStringExtra(UsuarioFormActivity.EXTRA_DOCUMENTO);
        String telefono = data.getStringExtra(UsuarioFormActivity.EXTRA_TELEFONO);
        String correo = data.getStringExtra(UsuarioFormActivity.EXTRA_CORREO);
        String rol = data.getStringExtra(UsuarioFormActivity.EXTRA_ROL);

        if (id == 0) {
            usuarios.add(new Usuario(siguienteId(), nombre, documento, telefono, correo, rol));
        } else {
            for (Usuario usuario : usuarios) {
                if (usuario.getId() == id) {
                    usuario.setNombre(nombre);
                    usuario.setDocumento(documento);
                    usuario.setTelefono(telefono);
                    usuario.setCorreo(correo);
                    usuario.setRol(rol);
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
        actualizarVacio();
    }

    private int siguienteId() {
        int maximo = 0;
        for (Usuario usuario : usuarios) {
            if (usuario.getId() > maximo) {
                maximo = usuario.getId();
            }
        }
        return maximo + 1;
    }

    @Override
    public void onEditar(Usuario usuario) {
        Intent intent = new Intent(requireContext(), UsuarioFormActivity.class);
        intent.putExtra(UsuarioFormActivity.EXTRA_ID, usuario.getId());
        intent.putExtra(UsuarioFormActivity.EXTRA_NOMBRE, usuario.getNombre());
        intent.putExtra(UsuarioFormActivity.EXTRA_DOCUMENTO, usuario.getDocumento());
        intent.putExtra(UsuarioFormActivity.EXTRA_TELEFONO, usuario.getTelefono());
        intent.putExtra(UsuarioFormActivity.EXTRA_CORREO, usuario.getCorreo());
        intent.putExtra(UsuarioFormActivity.EXTRA_ROL, usuario.getRol());
        formLauncher.launch(intent);
    }

    @Override
    public void onEliminar(Usuario usuario) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_usuario_titulo)
                .setMessage(getString(R.string.eliminar_usuario_mensaje, usuario.getNombre()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) -> {
                    // TODO-API: DELETE /usuarios/{id}
                    // Envía: id del usuario. Recibe: confirmación de eliminación.
                    // Quitar el usuario de la lista solo cuando la API confirme.
                    usuarios.remove(usuario);
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                    Toast.makeText(requireContext(), R.string.usuario_eliminado,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}