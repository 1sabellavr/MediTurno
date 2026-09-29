package co.edu.ue.mediturno.ui.citas;

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
import co.edu.ue.mediturno.adapter.CitaAdapter;
import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.util.NotificacionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CitasFragment extends Fragment implements CitaAdapter.OnCitaListener {

    private final List<Cita> citas = new ArrayList<>();
    private CitaAdapter adapter;
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
        View vista = inflater.inflate(R.layout.fragment_citas, container, false);

        RecyclerView rvCitas = vista.findViewById(R.id.rvCitas);
        tvVacio = vista.findViewById(R.id.tvVacio);
        ExtendedFloatingActionButton fabNueva = vista.findViewById(R.id.fabNuevaCita);

        adapter = new CitaAdapter(citas, this);
        rvCitas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCitas.setAdapter(adapter);

        fabNueva.setOnClickListener(v -> formLauncher.launch(
                new Intent(requireContext(), CitaFormActivity.class)));

        actualizarVacio();
        return vista;
    }

    private void cargarDatosDePrueba() {
        // TODO-API: GET /citas
        // Recibe: lista de Cita (id, paciente, médico, fecha, hora, motivo y estado).
        // Reemplazar estos datos de prueba por la respuesta de la API.
        // Al cargar las citas reales, volver a programar los recordatorios de las citas
        // futuras con NotificacionHelper.programarRecordatorio(), porque las alarmas
        // locales se pierden si el teléfono se reinicia.
        citas.add(new Cita(1, "María Rodríguez", "Dr. Carlos Pérez", "05/10/2026", "09:30",
                "Control general", Cita.ESTADO_PROGRAMADA));
        citas.add(new Cita(2, "Andrés Torres", "Dra. Laura Gómez", "07/10/2026", "14:00",
                "Dolor de cabeza frecuente", Cita.ESTADO_PROGRAMADA));
        citas.add(new Cita(3, "Sofía Herrera", "Dr. Carlos Pérez", "10/10/2026", "11:15",
                "Revisión de resultados de laboratorio", Cita.ESTADO_CANCELADA));
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(citas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void procesarResultado(Intent data) {
        int id = data.getIntExtra(CitaFormActivity.EXTRA_ID, 0);
        String paciente = data.getStringExtra(CitaFormActivity.EXTRA_PACIENTE);
        String medico = data.getStringExtra(CitaFormActivity.EXTRA_MEDICO);
        String fecha = data.getStringExtra(CitaFormActivity.EXTRA_FECHA);
        String hora = data.getStringExtra(CitaFormActivity.EXTRA_HORA);
        String motivo = data.getStringExtra(CitaFormActivity.EXTRA_MOTIVO);

        Cita citaGuardada = null;

        if (id == 0) {
            citaGuardada = new Cita(siguienteId(), paciente, medico, fecha, hora, motivo,
                    Cita.ESTADO_PROGRAMADA);
            citas.add(citaGuardada);
        } else {
            for (Cita cita : citas) {
                if (cita.getId() == id) {
                    cita.setPaciente(paciente);
                    cita.setMedico(medico);
                    cita.setFecha(fecha);
                    cita.setHora(hora);
                    cita.setMotivo(motivo);
                    citaGuardada = cita;
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
        actualizarVacio();

        if (citaGuardada != null) {
            programarRecordatorio(citaGuardada);
        }
    }

    private void programarRecordatorio(Cita cita) {
        boolean programado = NotificacionHelper.programarRecordatorio(requireContext(), cita);
        Toast.makeText(requireContext(),
                programado ? R.string.recordatorio_programado
                        : R.string.recordatorio_no_programado,
                Toast.LENGTH_LONG).show();
    }

    private int siguienteId() {
        int maximo = 0;
        for (Cita cita : citas) {
            if (cita.getId() > maximo) {
                maximo = cita.getId();
            }
        }
        return maximo + 1;
    }

    @Override
    public void onReprogramar(Cita cita) {
        Intent intent = new Intent(requireContext(), CitaFormActivity.class);
        intent.putExtra(CitaFormActivity.EXTRA_ID, cita.getId());
        intent.putExtra(CitaFormActivity.EXTRA_PACIENTE, cita.getPaciente());
        intent.putExtra(CitaFormActivity.EXTRA_MEDICO, cita.getMedico());
        intent.putExtra(CitaFormActivity.EXTRA_FECHA, cita.getFecha());
        intent.putExtra(CitaFormActivity.EXTRA_HORA, cita.getHora());
        intent.putExtra(CitaFormActivity.EXTRA_MOTIVO, cita.getMotivo());
        formLauncher.launch(intent);
    }

    @Override
    public void onCancelar(Cita cita) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancelar_cita_titulo)
                .setMessage(getString(R.string.cancelar_cita_mensaje, cita.getPaciente()))
                .setNegativeButton(R.string.btn_volver, null)
                .setPositiveButton(R.string.btn_cancelar_cita, (dialog, which) -> {
                    // TODO-API: PUT /citas/{id}/cancelar
                    // Envía: id de la cita. Recibe: la cita con estado CANCELADA.
                    // Cambiar el estado en la lista solo cuando la API confirme.
                    cita.setEstado(Cita.ESTADO_CANCELADA);
                    NotificacionHelper.cancelarRecordatorio(requireContext(), cita.getId());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), R.string.cita_cancelada,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}