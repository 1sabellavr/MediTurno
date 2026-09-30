package co.edu.ue.mediturno.ui.citas;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.model.PuntoAtencion;
import co.edu.ue.mediturno.util.NotificacionHelper;
import co.edu.ue.mediturno.util.UbicacionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CitaFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_PACIENTE = "paciente";
    public static final String EXTRA_MEDICO = "medico";
    public static final String EXTRA_FECHA = "fecha";
    public static final String EXTRA_HORA = "hora";
    public static final String EXTRA_MOTIVO = "motivo";
    public static final String EXTRA_PUNTO_ID = "punto_id";

    private TextView tvTituloForm;
    private TextView tvDireccionPunto;
    private TextView tvDistancia;
    private TextInputLayout tilPaciente;
    private TextInputLayout tilMedico;
    private TextInputLayout tilPunto;
    private TextInputLayout tilFecha;
    private TextInputLayout tilHora;
    private TextInputLayout tilMotivo;
    private TextInputEditText etPaciente;
    private TextInputEditText etMedico;
    private TextInputEditText etFecha;
    private TextInputEditText etHora;
    private TextInputEditText etMotivo;
    private MaterialAutoCompleteTextView actvPunto;
    private MaterialButton btnSedeCercana;
    private MaterialButton btnCancelar;
    private MaterialButton btnGuardar;

    private int idCita;
    private boolean modoEdicion;
    private final List<PuntoAtencion> puntos = new ArrayList<>();
    private ArrayAdapter<String> adaptadorPuntos;
    private PuntoAtencion puntoSeleccionado;
    private Location ubicacionActual;
    private SimpleDateFormat formatoFecha;

    private final ActivityResultLauncher<String[]> permisoUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    resultado -> {
                        if (UbicacionHelper.tienePermiso(this)) {
                            buscarSedeCercana();
                        } else {
                            Toast.makeText(this, R.string.ubicacion_permiso_denegado,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita_form);

        // El selector de fechas trabaja en UTC, por eso el formato también.
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        formatoFecha.setTimeZone(TimeZone.getTimeZone("UTC"));

        inicializarVistas();
        configurarPuntosAtencion();
        cargarDatosSiEsEdicion();
        configurarEventos();
    }

    private void inicializarVistas() {
        tvTituloForm = findViewById(R.id.tvTituloForm);
        tvDireccionPunto = findViewById(R.id.tvDireccionPunto);
        tvDistancia = findViewById(R.id.tvDistancia);
        tilPaciente = findViewById(R.id.tilPaciente);
        tilMedico = findViewById(R.id.tilMedico);
        tilPunto = findViewById(R.id.tilPunto);
        tilFecha = findViewById(R.id.tilFecha);
        tilHora = findViewById(R.id.tilHora);
        tilMotivo = findViewById(R.id.tilMotivo);
        etPaciente = findViewById(R.id.etPaciente);
        etMedico = findViewById(R.id.etMedico);
        actvPunto = findViewById(R.id.actvPunto);
        btnSedeCercana = findViewById(R.id.btnSedeCercana);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etMotivo = findViewById(R.id.etMotivo);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void configurarPuntosAtencion() {
        adaptadorPuntos = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        actvPunto.setAdapter(adaptadorPuntos);
        actvPunto.setOnItemClickListener((parent, view, position, id) ->
                seleccionarPunto(puntos.get(position)));

        cargarPuntosAtencion();
    }

    // GET /api/puntos-atencion
    private void cargarPuntosAtencion() {
        ApiClient.getApiService().obtenerPuntosAtencion().enqueue(new Callback<List<PuntoAtencion>>() {
            @Override
            public void onResponse(@NonNull Call<List<PuntoAtencion>> call,
                                   @NonNull Response<List<PuntoAtencion>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    puntos.clear();
                    puntos.addAll(response.body());

                    adaptadorPuntos.clear();
                    for (PuntoAtencion punto : puntos) {
                        adaptadorPuntos.add(punto.getNombre());
                    }
                    adaptadorPuntos.notifyDataSetChanged();

                    preseleccionarPuntoSiEsEdicion();
                } else {
                    Toast.makeText(CitaFormActivity.this,
                            ApiErrores.mensaje(CitaFormActivity.this, response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PuntoAtencion>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(CitaFormActivity.this, R.string.error_conexion,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Al reprogramar, se deja elegido el punto de atención que ya tenía la cita.
    private void preseleccionarPuntoSiEsEdicion() {
        if (!modoEdicion || puntoSeleccionado != null) {
            return;
        }
        int idPunto = getIntent().getIntExtra(EXTRA_PUNTO_ID, 0);
        for (PuntoAtencion punto : puntos) {
            if (punto.getId() == idPunto) {
                actvPunto.setText(punto.getNombre(), false);
                seleccionarPunto(punto);
                break;
            }
        }
    }

    private void seleccionarPunto(PuntoAtencion punto) {
        puntoSeleccionado = punto;
        tilPunto.setError(null);
        tvDireccionPunto.setText(punto.getDireccion());
        tvDireccionPunto.setVisibility(View.VISIBLE);
        actualizarDistancia();
    }

    private void actualizarDistancia() {
        if (ubicacionActual != null && puntoSeleccionado != null) {
            float metros = UbicacionHelper.distanciaMetros(ubicacionActual, puntoSeleccionado);
            tvDistancia.setText(getString(R.string.distancia_a_ti,
                    UbicacionHelper.formatearDistancia(metros)));
            tvDistancia.setVisibility(View.VISIBLE);
        }
    }

    private void cargarDatosSiEsEdicion() {
        Intent intent = getIntent();
        idCita = intent.getIntExtra(EXTRA_ID, 0);
        modoEdicion = idCita != 0;

        if (modoEdicion) {
            tvTituloForm.setText(R.string.cita_form_titulo_editar);
            etPaciente.setText(intent.getStringExtra(EXTRA_PACIENTE));
            etMedico.setText(intent.getStringExtra(EXTRA_MEDICO));
            etFecha.setText(intent.getStringExtra(EXTRA_FECHA));
            etHora.setText(intent.getStringExtra(EXTRA_HORA));
            etMotivo.setText(intent.getStringExtra(EXTRA_MOTIVO));
        } else {
            tvTituloForm.setText(R.string.cita_form_titulo_crear);
        }
    }

    private void configurarEventos() {
        etFecha.setOnClickListener(v -> mostrarSelectorFecha());
        tilFecha.setEndIconOnClickListener(v -> mostrarSelectorFecha());
        etHora.setOnClickListener(v -> mostrarSelectorHora());
        tilHora.setEndIconOnClickListener(v -> mostrarSelectorHora());
        btnSedeCercana.setOnClickListener(v -> solicitarSedeCercana());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    // ---------- GPS del dispositivo ----------

    private void solicitarSedeCercana() {
        if (puntos.isEmpty()) {
            Toast.makeText(this, R.string.error_conexion, Toast.LENGTH_LONG).show();
            return;
        }
        if (UbicacionHelper.tienePermiso(this)) {
            buscarSedeCercana();
        } else {
            permisoUbicacion.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void buscarSedeCercana() {
        tvDistancia.setText(R.string.ubicacion_obteniendo);
        tvDistancia.setVisibility(View.VISIBLE);
        btnSedeCercana.setEnabled(false);

        UbicacionHelper.obtenerUbicacion(this, new UbicacionHelper.Callback() {
            @Override
            public void onUbicacion(Location ubicacion) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnSedeCercana.setEnabled(true);
                ubicacionActual = ubicacion;

                PuntoAtencion cercano = UbicacionHelper.masCercano(ubicacion, puntos);
                if (cercano != null) {
                    actvPunto.setText(cercano.getNombre(), false);
                    seleccionarPunto(cercano);

                    float metros = UbicacionHelper.distanciaMetros(ubicacion, cercano);
                    tvDistancia.setText(getString(R.string.sede_cercana_elegida,
                            cercano.getNombre(), UbicacionHelper.formatearDistancia(metros)));
                    tvDistancia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnSedeCercana.setEnabled(true);
                tvDistancia.setVisibility(View.GONE);
                Toast.makeText(CitaFormActivity.this, R.string.ubicacion_no_disponible,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------- Fecha y hora ----------

    private void mostrarSelectorFecha() {
        // Solo se pueden elegir fechas desde hoy en adelante.
        CalendarConstraints restricciones = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker.Builder<Long> constructor = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.fecha_cita_picker_titulo)
                .setCalendarConstraints(restricciones);

        Long seleccionActual = fechaActualEnMillis();
        if (seleccionActual != null) {
            constructor.setSelection(seleccionActual);
        }

        MaterialDatePicker<Long> selector = constructor.build();
        selector.addOnPositiveButtonClickListener(millis ->
                etFecha.setText(formatoFecha.format(new Date(millis))));
        selector.show(getSupportFragmentManager(), "fecha_cita");
    }

    private Long fechaActualEnMillis() {
        String texto = texto(etFecha);
        if (texto.isEmpty()) {
            return null;
        }
        try {
            Date fecha = formatoFecha.parse(texto);
            return fecha != null ? fecha.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private void mostrarSelectorHora() {
        int hora = 8;
        int minuto = 0;
        String actual = texto(etHora);
        if (actual.matches("\\d{2}:\\d{2}")) {
            hora = Integer.parseInt(actual.substring(0, 2));
            minuto = Integer.parseInt(actual.substring(3, 5));
        }

        MaterialTimePicker selector = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hora)
                .setMinute(minuto)
                .setTitleText(R.string.hora_picker_titulo)
                .build();

        selector.addOnPositiveButtonClickListener(v ->
                etHora.setText(String.format(Locale.US, "%02d:%02d",
                        selector.getHour(), selector.getMinute())));
        selector.show(getSupportFragmentManager(), "hora_cita");
    }

    // ---------- Guardar y validar ----------

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void guardar() {
        String paciente = texto(etPaciente);
        String medico = texto(etMedico);
        String fecha = texto(etFecha);
        String hora = texto(etHora);
        String motivo = texto(etMotivo);

        if (!validarCampos(paciente, medico, fecha, hora, motivo)) {
            return;
        }

        Cita cita = new Cita(idCita, paciente, medico, fecha, hora, motivo,
                Cita.ESTADO_PROGRAMADA, puntoSeleccionado);
        btnGuardar.setEnabled(false);

        // POST /api/citas (agendar) o PUT /api/citas/{id} (reprogramar)
        Call<Cita> llamada = modoEdicion
                ? ApiClient.getApiService().actualizarCita(idCita, cita)
                : ApiClient.getApiService().crearCita(cita);

        llamada.enqueue(new Callback<Cita>() {
            @Override
            public void onResponse(@NonNull Call<Cita> call, @NonNull Response<Cita> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    programarRecordatorio(response.body());
                    setResult(RESULT_OK);
                    Toast.makeText(CitaFormActivity.this, R.string.cita_guardada,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnGuardar.setEnabled(true);
                    // Por ejemplo: el médico ya tiene una cita en esa fecha y hora (409).
                    Toast.makeText(CitaFormActivity.this,
                            ApiErrores.mensaje(CitaFormActivity.this, response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cita> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnGuardar.setEnabled(true);
                Toast.makeText(CitaFormActivity.this, R.string.error_conexion,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Recordatorio de la cita en el teléfono. Primero se cancela el anterior, por si la
    // cita se reprogramó y el aviso nuevo ya no cabe (queda a menos de una hora).
    private void programarRecordatorio(Cita cita) {
        NotificacionHelper.cancelarRecordatorio(getApplicationContext(), cita.getId());
        boolean programado = NotificacionHelper.programarRecordatorio(
                getApplicationContext(), cita);
        Toast.makeText(getApplicationContext(),
                programado ? R.string.recordatorio_programado
                        : R.string.recordatorio_no_programado,
                Toast.LENGTH_LONG).show();
    }

    private boolean validarCampos(String paciente, String medico, String fecha,
                                  String hora, String motivo) {
        boolean valido = true;

        tilPaciente.setError(null);
        tilMedico.setError(null);
        tilPunto.setError(null);
        tilFecha.setError(null);
        tilHora.setError(null);
        tilMotivo.setError(null);

        if (paciente.isEmpty()) {
            tilPaciente.setError(getString(R.string.error_paciente_vacio));
            valido = false;
        }

        if (medico.isEmpty()) {
            tilMedico.setError(getString(R.string.error_medico_vacio));
            valido = false;
        }

        if (puntoSeleccionado == null) {
            tilPunto.setError(getString(R.string.error_punto_vacio));
            valido = false;
        }

        if (fecha.isEmpty()) {
            tilFecha.setError(getString(R.string.error_fecha_cita_vacia));
            valido = false;
        }

        if (hora.isEmpty()) {
            tilHora.setError(getString(R.string.error_hora_cita_vacia));
            valido = false;
        }

        if (!fecha.isEmpty() && !hora.isEmpty() && esFechaHoraPasada(fecha, hora)) {
            tilHora.setError(getString(R.string.error_cita_pasada));
            valido = false;
        }

        if (motivo.isEmpty()) {
            tilMotivo.setError(getString(R.string.error_motivo_vacio));
            valido = false;
        }

        return valido;
    }

    private boolean esFechaHoraPasada(String fecha, String hora) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            Date fechaHora = formato.parse(fecha + " " + hora);
            return fechaHora != null && fechaHora.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }
}