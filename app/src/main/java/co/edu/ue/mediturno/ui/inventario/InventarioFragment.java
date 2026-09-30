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
import co.edu.ue.mediturno.api.ApiErrores;
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

        // Cuando el formulario guarda con éxito, se vuelve a pedir la lista a la API.
        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarInventario();
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

        cargarInventario();
        return vista;
    }

    // GET /api/inventario
    private void cargarInventario() {
        ApiClient.getApiService().obtenerInventario().enqueue(new Callback<List<Medicamento>>() {
            @Override
            public void onResponse(@NonNull Call<List<Medicamento>> call,
                                   @NonNull Response<List<Medicamento>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    medicamentos.clear();
                    medicamentos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                } else {
                    mostrarMensaje(ApiErrores.mensaje(requireContext(), response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Medicamento>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarMensaje(getString(R.string.error_conexion));
                }
            }
        });
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(medicamentos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
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

    @Override
    public void onEliminar(Medicamento medicamento) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_medicamento_titulo)
                .setMessage(getString(R.string.eliminar_medicamento_mensaje,
                        medicamento.getNombre()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_eliminar,
                        (dialog, which) -> eliminarMedicamento(medicamento))
                .show();
    }

    // DELETE /api/inventario/{id}
    private void eliminarMedicamento(Medicamento medicamento) {
        ApiClient.getApiService().eliminarMedicamento(medicamento.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call,
                                           @NonNull Response<Void> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful()) {
                            medicamentos.remove(medicamento);
                            adapter.notifyDataSetChanged();
                            actualizarVacio();
                            mostrarMensaje(getString(R.string.medicamento_eliminado));
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