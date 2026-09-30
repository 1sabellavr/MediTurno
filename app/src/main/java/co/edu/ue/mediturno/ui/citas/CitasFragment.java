package co.edu.ue.mediturno.ui.citas;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.model.PuntoAtencion;
import co.edu.ue.mediturno.util.NotificacionHelper;
import co.edu.ue.mediturno.util.UbicacionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CitasFragment extends Fragment implements CitaAdapter.OnCitaListener {

    // Evita volver a pedir el permiso cada vez que se entra a la pestaña.
    private static boolean permisoUbicacionSolicitado = false;

    private final List<Cita> citas = new ArrayList<>();
    private CitaAdapter adapter;
    private TextView tvVacio;
    private ActivityResultLauncher<Intent> formLauncher;
    private ActivityResultLauncher<String[]> permisoUbicacion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Cuando el formulario guarda con éxito, se vuelve a pedir la lista a la API.
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarCitas();
                    }
                });

        permisoUbicacion = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                resultado -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (UbicacionHelper.tienePermiso(requireContext())) {
                        leerUbicacionDelDispositivo();
                    } else {
                        Toast.makeText(requireContext(), R.string.ubicacion_permiso_denegado,
                                Toast.LENGTH_LONG).show();
                    }
                });
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

        cargarCitas();
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iniciarUbicacion();
    }

    // ---------- GPS del dispositivo ----------

    private void iniciarUbicacion() {
        if (UbicacionHelper.tienePermiso(requireContext())) {
            leerUbicacionDelDispositivo();
        } else if (!permisoUbicacionSolicitado) {
            permisoUbicacionSolicitado = true;
            permisoUbicacion.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void leerUbicacionDelDispositivo() {
        UbicacionHelper.obtenerUbicacion(requireContext(), new UbicacionHelper.Callback() {
            @Override
            public void onUbicacion(Location ubicacion) {
                if (isAdded()) {
                    adapter.setUbicacion(ubicacion);
                }
            }

            @Override
            public void onError() {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.ubicacion_no_disponible,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // ---------- Datos (API) ----------

    // GET /api/citas
    private void cargarCitas() {
        ApiClient.getApiService().obtenerCitas().enqueue(new Callback<List<Cita>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cita>> call,
                                   @NonNull Response<List<Cita>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    citas.clear();
                    citas.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    actualizarVacio();
                } else {
                    mostrarMensaje(ApiErrores.mensaje(requireContext(), response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cita>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarMensaje(getString(R.string.error_conexion));
                }
            }
        });
    }

    private void actualizarVacio() {
        tvVacio.setVisibility(citas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    // ---------- Acciones de cada tarjeta ----------

    @Override
    public void onReprogramar(Cita cita) {
        Intent intent = new Intent(requireContext(), CitaFormActivity.class);
        intent.putExtra(CitaFormActivity.EXTRA_ID, cita.getId());
        intent.putExtra(CitaFormActivity.EXTRA_PACIENTE, cita.getPaciente());
        intent.putExtra(CitaFormActivity.EXTRA_MEDICO, cita.getMedico());
        intent.putExtra(CitaFormActivity.EXTRA_FECHA, cita.getFecha());
        intent.putExtra(CitaFormActivity.EXTRA_HORA, cita.getHora());
        intent.putExtra(CitaFormActivity.EXTRA_MOTIVO, cita.getMotivo());
        if (cita.getPuntoAtencion() != null) {
            intent.putExtra(CitaFormActivity.EXTRA_PUNTO_ID, cita.getPuntoAtencion().getId());
        }
        formLauncher.launch(intent);
    }

    @Override
    public void onCancelar(Cita cita) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancelar_cita_titulo)
                .setMessage(getString(R.string.cancelar_cita_mensaje, cita.getPaciente()))
                .setNegativeButton(R.string.btn_volver, null)
                .setPositiveButton(R.string.btn_cancelar_cita,
                        (dialog, which) -> cancelarCita(cita))
                .show();
    }

    // PUT /api/citas/{id}/cancelar (la cita no se borra, cambia de estado)
    private void cancelarCita(Cita cita) {
        ApiClient.getApiService().cancelarCita(cita.getId()).enqueue(new Callback<Cita>() {
            @Override
            public void onResponse(@NonNull Call<Cita> call, @NonNull Response<Cita> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    cita.setEstado(Cita.ESTADO_CANCELADA);
                    NotificacionHelper.cancelarRecordatorio(requireContext(), cita.getId());
                    adapter.notifyDataSetChanged();
                    mostrarMensaje(getString(R.string.cita_cancelada));
                } else {
                    mostrarMensaje(ApiErrores.mensaje(requireContext(), response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cita> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarMensaje(getString(R.string.error_conexion));
                }
            }
        });
    }

    @Override
    public void onVerMapa(Cita cita) {
        PuntoAtencion punto = cita.getPuntoAtencion();
        if (punto == null) {
            return;
        }

        String coordenadas = String.format(Locale.US, "%f,%f",
                punto.getLatitud(), punto.getLongitud());
        Uri uri = Uri.parse("geo:" + coordenadas + "?q=" + coordenadas
                + "(" + Uri.encode(punto.getNombre()) + ")");

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.sin_app_mapas, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onComoLlegar(Cita cita) {
        PuntoAtencion punto = cita.getPuntoAtencion();
        if (punto == null) {
            return;
        }

        String destino = String.format(Locale.US, "%f,%f",
                punto.getLatitud(), punto.getLongitud());

        // La app de mapas usa el GPS del dispositivo como punto de partida.
        Intent navegacion = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + destino));
        navegacion.setPackage("com.google.android.apps.maps");

        try {
            startActivity(navegacion);
        } catch (ActivityNotFoundException e) {
            Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=" + destino));
            try {
                startActivity(web);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(requireContext(), R.string.sin_app_mapas,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}