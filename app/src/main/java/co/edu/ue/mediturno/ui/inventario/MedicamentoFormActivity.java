package co.edu.ue.mediturno.ui.inventario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MedicamentoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_DESCRIPCION = "descripcion";
    public static final String EXTRA_CANTIDAD = "cantidad";
    public static final String EXTRA_VENCIMIENTO = "vencimiento";

    private TextView tvTituloForm;
    private TextInputLayout tilNombre;
    private TextInputLayout tilCantidad;
    private TextInputLayout tilVencimiento;
    private TextInputEditText etNombre;
    private TextInputEditText etDescripcion;
    private TextInputEditText etCantidad;
    private TextInputEditText etVencimiento;
    private MaterialButton btnCancelar;
    private MaterialButton btnGuardar;

    private int idMedicamento;
    private boolean modoEdicion;
    private SimpleDateFormat formatoFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicamento_form);

        // El selector de fechas trabaja en UTC, por eso el formato también.
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        formatoFecha.setTimeZone(TimeZone.getTimeZone("UTC"));

        inicializarVistas();
        cargarDatosSiEsEdicion();
        configurarEventos();
    }

    private void inicializarVistas() {
        tvTituloForm = findViewById(R.id.tvTituloForm);
        tilNombre = findViewById(R.id.tilNombre);
        tilCantidad = findViewById(R.id.tilCantidad);
        tilVencimiento = findViewById(R.id.tilVencimiento);
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
        etVencimiento = findViewById(R.id.etVencimiento);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void cargarDatosSiEsEdicion() {
        Intent intent = getIntent();
        idMedicamento = intent.getIntExtra(EXTRA_ID, 0);
        modoEdicion = idMedicamento != 0;

        if (modoEdicion) {
            tvTituloForm.setText(R.string.medicamento_form_titulo_editar);
            etNombre.setText(intent.getStringExtra(EXTRA_NOMBRE));
            etDescripcion.setText(intent.getStringExtra(EXTRA_DESCRIPCION));
            etCantidad.setText(String.valueOf(intent.getIntExtra(EXTRA_CANTIDAD, 0)));
            etVencimiento.setText(intent.getStringExtra(EXTRA_VENCIMIENTO));
        } else {
            tvTituloForm.setText(R.string.medicamento_form_titulo_crear);
        }
    }

    private void configurarEventos() {
        etVencimiento.setOnClickListener(v -> mostrarSelectorFecha());
        tilVencimiento.setEndIconOnClickListener(v -> mostrarSelectorFecha());
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void mostrarSelectorFecha() {
        MaterialDatePicker.Builder<Long> constructor = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.fecha_picker_titulo);

        Long seleccionActual = fechaActualEnMillis();
        if (seleccionActual != null) {
            constructor.setSelection(seleccionActual);
        }

        MaterialDatePicker<Long> selector = constructor.build();
        selector.addOnPositiveButtonClickListener(millis ->
                etVencimiento.setText(formatoFecha.format(new Date(millis))));
        selector.show(getSupportFragmentManager(), "fecha_vencimiento");
    }

    private Long fechaActualEnMillis() {
        String texto = texto(etVencimiento);
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

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void guardar() {
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);
        String cantidadTexto = texto(etCantidad);
        String vencimiento = texto(etVencimiento);

        if (!validarCampos(nombre, cantidadTexto, vencimiento)) {
            return;
        }

        int cantidad = Integer.parseInt(cantidadTexto);

        // TODO-API: si modoEdicion es false -> POST /medicamentos (envía nombre, descripción,
        // cantidad y fecha de vencimiento). Si es true -> PUT /medicamentos/{id}
        // (envía los mismos campos, incluida la cantidad actualizada).
        // Reemplazar el resultado de prueba de abajo por la respuesta real de la API
        // y mostrar el error si el medicamento ya existe.
        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_ID, idMedicamento);
        resultado.putExtra(EXTRA_NOMBRE, nombre);
        resultado.putExtra(EXTRA_DESCRIPCION, descripcion);
        resultado.putExtra(EXTRA_CANTIDAD, cantidad);
        resultado.putExtra(EXTRA_VENCIMIENTO, vencimiento);
        setResult(RESULT_OK, resultado);

        Toast.makeText(this, R.string.medicamento_guardado, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validarCampos(String nombre, String cantidadTexto, String vencimiento) {
        boolean valido = true;

        tilNombre.setError(null);
        tilCantidad.setError(null);
        tilVencimiento.setError(null);

        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.error_medicamento_nombre_vacio));
            valido = false;
        }

        if (cantidadTexto.isEmpty()) {
            tilCantidad.setError(getString(R.string.error_cantidad_vacia));
            valido = false;
        } else if (!cantidadTexto.matches("\\d{1,9}")) {
            tilCantidad.setError(getString(R.string.error_cantidad_invalida));
            valido = false;
        }

        if (vencimiento.isEmpty()) {
            tilVencimiento.setError(getString(R.string.error_vencimiento_vacio));
            valido = false;
        }

        return valido;
    }
}