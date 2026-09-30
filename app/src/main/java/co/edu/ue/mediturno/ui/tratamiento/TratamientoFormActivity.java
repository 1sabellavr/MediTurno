package co.edu.ue.mediturno.ui.tratamiento;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Tratamiento;
import co.edu.ue.mediturno.repository.TratamientoRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class TratamientoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_MEDICAMENTO = "medicamento";
    public static final String EXTRA_DOSIS = "dosis";
    public static final String EXTRA_HORA = "hora";
    public static final String EXTRA_NOTAS = "notas";

    private TextView tvTituloForm;
    private TextInputLayout tilMedicamento;
    private TextInputLayout tilDosis;
    private TextInputLayout tilHoraToma;
    private TextInputEditText etMedicamento;
    private TextInputEditText etDosis;
    private TextInputEditText etHoraToma;
    private TextInputEditText etNotas;
    private MaterialButton btnCancelar;
    private MaterialButton btnGuardar;

    private TratamientoRepository repositorio;
    private int idTratamiento;
    private boolean modoEdicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tratamiento_form);

        repositorio = new TratamientoRepository(this);

        inicializarVistas();
        cargarDatosSiEsEdicion();
        configurarEventos();
    }

    private void inicializarVistas() {
        tvTituloForm = findViewById(R.id.tvTituloForm);
        tilMedicamento = findViewById(R.id.tilMedicamento);
        tilDosis = findViewById(R.id.tilDosis);
        tilHoraToma = findViewById(R.id.tilHoraToma);
        etMedicamento = findViewById(R.id.etMedicamento);
        etDosis = findViewById(R.id.etDosis);
        etHoraToma = findViewById(R.id.etHoraToma);
        etNotas = findViewById(R.id.etNotas);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void cargarDatosSiEsEdicion() {
        Intent intent = getIntent();
        idTratamiento = intent.getIntExtra(EXTRA_ID, 0);
        modoEdicion = idTratamiento != 0;

        if (modoEdicion) {
            tvTituloForm.setText(R.string.tratamiento_form_titulo_editar);
            etMedicamento.setText(intent.getStringExtra(EXTRA_MEDICAMENTO));
            etDosis.setText(intent.getStringExtra(EXTRA_DOSIS));
            etHoraToma.setText(intent.getStringExtra(EXTRA_HORA));
            etNotas.setText(intent.getStringExtra(EXTRA_NOTAS));
        } else {
            tvTituloForm.setText(R.string.tratamiento_form_titulo_crear);
        }
    }

    private void configurarEventos() {
        etHoraToma.setOnClickListener(v -> mostrarSelectorHora());
        tilHoraToma.setEndIconOnClickListener(v -> mostrarSelectorHora());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void mostrarSelectorHora() {
        int hora = 8;
        int minuto = 0;
        String actual = texto(etHoraToma);
        if (actual.matches("\\d{2}:\\d{2}")) {
            hora = Integer.parseInt(actual.substring(0, 2));
            minuto = Integer.parseInt(actual.substring(3, 5));
        }

        MaterialTimePicker selector = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hora)
                .setMinute(minuto)
                .setTitleText(R.string.hora_toma_picker_titulo)
                .build();

        selector.addOnPositiveButtonClickListener(v ->
                etHoraToma.setText(String.format(Locale.US, "%02d:%02d",
                        selector.getHour(), selector.getMinute())));
        selector.show(getSupportFragmentManager(), "hora_toma");
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void guardar() {
        String medicamento = texto(etMedicamento);
        String dosis = texto(etDosis);
        String hora = texto(etHoraToma);
        String notas = texto(etNotas);

        if (!validarCampos(medicamento, dosis, hora)) {
            return;
        }

        Tratamiento tratamiento = new Tratamiento(idTratamiento, medicamento, dosis, hora, notas);

        boolean guardado;
        if (modoEdicion) {
            // UPDATE en SQLite
            guardado = repositorio.actualizar(tratamiento) > 0;
        } else {
            // INSERT en SQLite (cada tratamiento queda asociado al correo de quien inició sesión)
            FirebaseUser actual = FirebaseAuth.getInstance().getCurrentUser();
            String usuario = actual != null && actual.getEmail() != null
                    ? actual.getEmail() : "";
            guardado = repositorio.insertar(tratamiento, usuario) != -1;
        }

        if (guardado) {
            setResult(RESULT_OK);
            Toast.makeText(this, R.string.tratamiento_guardado, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_bd_local, Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos(String medicamento, String dosis, String hora) {
        boolean valido = true;

        tilMedicamento.setError(null);
        tilDosis.setError(null);
        tilHoraToma.setError(null);

        if (medicamento.isEmpty()) {
            tilMedicamento.setError(getString(R.string.error_tratamiento_medicamento_vacio));
            valido = false;
        }

        if (dosis.isEmpty()) {
            tilDosis.setError(getString(R.string.error_dosis_vacia));
            valido = false;
        }

        if (hora.isEmpty()) {
            tilHoraToma.setError(getString(R.string.error_hora_toma_vacia));
            valido = false;
        }

        return valido;
    }
}