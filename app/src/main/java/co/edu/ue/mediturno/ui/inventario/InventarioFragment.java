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
import co.edu.ue.mediturno.model.Medicamento;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

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

        cargarDatosDePrueba();
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
        return vista;
    }

    private void cargarDatosDePrueba() {
        // TODO-API: GET /medicamentos
        // Recibe: lista de Medicamento (id, nombre, descripción, cantidad y fecha de vencimiento).
        // Reemplazar estos datos de prueba por la respuesta de la API.
        medicamentos.add(new Medicamento(1, "Acetaminofén 500 mg",
                "Analgésico y antipirético, caja x 20 tabletas", 120, "15/03/2027"));
        medicamentos.add(new Medicamento(2, "Ibuprofeno 400 mg",
                "Antiinflamatorio, caja x 10 tabletas", 8, "30/11/2026"));
        medicamentos.add(new Medicamento(3, "Amoxicilina 500 mg",
                "Antibiótico, caja x 12 cápsulas", 45, "10/08/2027"));
        medicamentos.add(new Medicamento(4, "Loratadina 10 mg",
                "Antihistamínico, caja x 10 tabletas", 60, "01/06/2027"));
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(medicamentos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void procesarResultado(Intent data) {
        int id = data.getIntExtra(MedicamentoFormActivity.EXTRA_ID, 0);
        String nombre = data.getStringExtra(MedicamentoFormActivity.EXTRA_NOMBRE);
        String descripcion = data.getStringExtra(MedicamentoFormActivity.EXTRA_DESCRIPCION);
        int cantidad = data.getIntExtra(MedicamentoFormActivity.EXTRA_CANTIDAD, 0);
        String vencimiento = data.getStringExtra(MedicamentoFormActivity.EXTRA_VENCIMIENTO);

        if (id == 0) {
            medicamentos.add(new Medicamento(siguienteId(), nombre, descripcion, cantidad,
                    vencimiento));
        } else {
            for (Medicamento medicamento : medicamentos) {
                if (medicamento.getId() == id) {
                    medicamento.setNombre(nombre);
                    medicamento.setDescripcion(descripcion);
                    medicamento.setCantidad(cantidad);
                    medicamento.setFechaVencimiento(vencimiento);
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
        actualizarVacio();
    }

    private int siguienteId() {
        int maximo = 0;
        for (Medicamento medicamento : medicamentos) {
            if (medicamento.getId() > maximo) {
                maximo = medicamento.getId();
            }
        }
        return maximo + 1;
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
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) -> {
                    // TODO-API: DELETE /medicamentos/{id}
                    // Envía: id del medicamento. Recibe: confirmación de eliminación.
                    // Quitar el medicamento de la lista solo cuando la API confirme.
                    medicamentos.remove(medicamento);
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                    Toast.makeText(requireContext(), R.string.medicamento_eliminado,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}