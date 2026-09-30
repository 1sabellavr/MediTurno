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
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Usuario;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuariosFragment extends Fragment implements UsuarioAdapter.OnUsuarioListener {

    private final List<Usuario> usuarios = new ArrayList<>();
    private UsuarioAdapter adapter;
    private TextView tvVacio;
    private ActivityResultLauncher<Intent> formLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cuando el formulario guarda con éxito, se vuelve a pedir la lista a la API.
        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarUsuarios();
                    }
                });
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

        cargarUsuarios();
        return vista;
    }

    // GET /api/usuarios
    private void cargarUsuarios() {
        ApiClient.getApiService().obtenerUsuarios().enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<List<Usuario>> call,
                                   @NonNull Response<List<Usuario>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    usuarios.clear();
                    usuarios.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                } else {
                    mostrarMensaje(ApiErrores.mensaje(requireContext(), response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Usuario>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarMensaje(getString(R.string.error_conexion));
                }
            }
        });
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(usuarios.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
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
                .setPositiveButton(R.string.btn_eliminar,
                        (dialog, which) -> eliminarUsuario(usuario))
                .show();
    }

    // DELETE /api/usuarios/{id}
    private void eliminarUsuario(Usuario usuario) {
        ApiClient.getApiService().eliminarUsuario(usuario.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call,
                                           @NonNull Response<Void> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful()) {
                            usuarios.remove(usuario);
                            adapter.notifyDataSetChanged();
                            actualizarVacio();
                            mostrarMensaje(getString(R.string.usuario_eliminado));
                        } else {
                            mostrarMensaje(ApiErrores.mensaje(requireContext(), response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            mostrarMensaje(getString(R.string.error_conexion));
                        }
                    }
                });
    }
}