package co.edu.ue.mediturno.ui.inventario;

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
import co.edu.ue.mediturno.adapter.MedicamentoAdapter;
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.model.Medicamento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventarioFragment extends Fragment
        implements MedicamentoAdapter.OnMedicamentoListener {

    private final List<Medicamento> medicamentos = new ArrayList<>();
    private MedicamentoAdapter adapter;
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_inventario, container, false);

        RecyclerView rvInventario = vista.findViewById(R.id.rvInventario);
        tvVacio = vista.findViewById(R.id.tvVacio);
        ExtendedFloatingActionButton fabNuevo = vista.findViewById(R.id.fabNuevoMedicamento);

        adapter = new MedicamentoAdapter(medicamentos, this);
        rvInventario.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInventario.setAdapter(adapter);

        fabNuevo.setOnClickListener(v -> formLauncher.launch(
                new Intent(requireContext(), MedicamentoFormActivity.class)));

        actualizarVacio();
        cargarMedicamentos();
        return vista;
    }

    // ---------- READ: GET api/inventario ----------
    private void cargarMedicamentos() {
        ApiClient.getApiService().obtenerInventario().enqueue(new Callback<List<Medicamento>>() {
            @Override
            public void onResponse(@NonNull Call<List<Medicamento>> call,
                                   @NonNull Response<List<Medicamento>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    medicamentos.clear();
                    medicamentos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                } else {
                    mostrarError("No se pudo cargar el inventario (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Medicamento>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(medicamentos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    // ---------- CREATE (POST) y UPDATE (PUT) ----------
    private void procesarResultado(Intent data) {
        int id = data.getIntExtra(MedicamentoFormActivity.EXTRA_ID, 0);
        String nombre = data.getStringExtra(MedicamentoFormActivity.EXTRA_NOMBRE);
        String descripcion = data.getStringExtra(MedicamentoFormActivity.EXTRA_DESCRIPCION);
        int cantidad = data.getIntExtra(MedicamentoFormActivity.EXTRA_CANTIDAD, 0);
        String vencimiento = data.getStringExtra(MedicamentoFormActivity.EXTRA_VENCIMIENTO);

        // si es edicion, partimos del medicamento que ya existe para no perder los demas campos
        Medicamento medicamento = null;
        if (id != 0) {
            for (Medicamento m : medicamentos) {
                if (m.getId() == id) {
                    medicamento = m;
                    break;
                }
            }
        }
        if (medicamento == null) {
            medicamento = new Medicamento();
        }
        medicamento.setNombre(nombre);
        medicamento.setDescripcion(descripcion);
        medicamento.setCantidad(cantidad);
        medicamento.setFechaVencimiento(vencimiento);

        Callback<Medicamento> respuesta = new Callback<Medicamento>() {
            @Override
            public void onResponse(@NonNull Call<Medicamento> call,
                                   @NonNull Response<Medicamento> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    cargarMedicamentos(); // recarga la lista desde la API
                } else {
                    mostrarError("No se pudo guardar (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Medicamento> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarError("Error de conexión: " + t.getMessage());
            }
        };

        if (id == 0) {
            ApiClient.getApiService().crearMedicamento(medicamento).enqueue(respuesta);
        } else {
            ApiClient.getApiService().actualizarMedicamento(id, medicamento).enqueue(respuesta);
        }
    }

    @Override
    public void onEditar(Medicamento medicamento) {
        Intent intent = new Intent(requireContext(), MedicamentoFormActivity.class);
        intent.putExtra(MedicamentoFormActivity.EXTRA_ID, medicamento.getId());
        intent.putExtra(MedicamentoFormActivity.EXTRA_NOMBRE, medicamento.getNombre());
        intent.putExtra(MedicamentoFormActivity.EXTRA_DESCRIPCION, medicamento.getDescripcion());
        intent.putExtra(MedicamentoFormActivity.EXTRA_CANTIDAD, medicamento.getCantidad());
        intent.putExtra(MedicamentoFormActivity.EXTRA_VENCIMIENTO,
                medicamento.getFechaVencimiento());
        formLauncher.launch(intent);
    }

    // ---------- DELETE: DELETE api/inventario/{id} ----------
    @Override
    public void onEliminar(Medicamento medicamento) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_medicamento_titulo)
                .setMessage(getString(R.string.eliminar_medicamento_mensaje,
                        medicamento.getNombre()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) ->
                        ApiClient.getApiService().eliminarMedicamento(medicamento.getId())
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(@NonNull Call<Void> call,
                                                           @NonNull Response<Void> response) {
                                        if (!isAdded()) return;
                                        if (response.isSuccessful()) {
                                            medicamentos.remove(medicamento);
                                            adapter.notifyDataSetChanged();
                                            actualizarVacio();
                                            Toast.makeText(requireContext(),
                                                    R.string.medicamento_eliminado,
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            mostrarError("No se pudo eliminar ("
                                                    + response.code() + ")");
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<Void> call,
                                                          @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        mostrarError("Error de conexión: " + t.getMessage());
                                    }
                                }))
                .show();
    }
}