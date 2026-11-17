package co.edu.ue.mediturno.ui.inventario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Medicamento;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    // Si llega un id distinto de 0 en el Intent, el formulario está en modo edición:
    // se rellenan los campos y luego se usará PUT en lugar de POST.
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

    /**
     * Guarda el medicamento en la API (Spring Boot + PostgreSQL).
     * - Modo creación -> POST /api/inventario
     * - Modo edición  -> PUT  /api/inventario/{id}
     * Solo se cierra el formulario cuando la API confirma; así la lista del inventario
     * (que se recarga desde la API) siempre muestra lo que realmente quedó guardado.
     */
    private void guardar() {
        String nombre = texto(etNombre);
        String descripcion = texto(etDescripcion);
        String cantidadTexto = texto(etCantidad);
        String vencimiento = texto(etVencimiento);

        if (!validarCampos(nombre, cantidadTexto, vencimiento)) {
            return;
        }

        int cantidad = Integer.parseInt(cantidadTexto);

        // Objeto que Retrofit convertirá a JSON con Gson para enviarlo en el cuerpo (@Body).
        Medicamento medicamento = new Medicamento(idMedicamento, nombre, descripcion,
                cantidad, vencimiento);

        // Se desactiva el botón para evitar enviar el mismo medicamento dos veces.
        btnGuardar.setEnabled(false);

        // Se elige la llamada según el modo: editar (PUT) o crear (POST).
        Call<Medicamento> llamada = modoEdicion
                ? ApiClient.getApiService().actualizarMedicamento(idMedicamento, medicamento)
                : ApiClient.getApiService().crearMedicamento(medicamento);

        // enqueue = petición asíncrona: no bloquea la pantalla mientras espera al servidor.
        llamada.enqueue(new Callback<Medicamento>() {

            // La API respondió (puede ser éxito 200/201 o un error 400/404/409...).
            @Override
            public void onResponse(@NonNull Call<Medicamento> call,
                                   @NonNull Response<Medicamento> response) {
                // Si el usuario ya salió de la pantalla, no se toca la interfaz.
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful()) {
                    // RESULT_OK avisa a InventarioFragment para que recargue la lista desde la API.
                    setResult(RESULT_OK);
                    Toast.makeText(MedicamentoFormActivity.this,
                            R.string.medicamento_guardado, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Error controlado de la API: se reactiva el botón y se muestra su mensaje.
                    btnGuardar.setEnabled(true);
                    Toast.makeText(MedicamentoFormActivity.this,
                            ApiErrores.mensaje(MedicamentoFormActivity.this, response),
                            Toast.LENGTH_LONG).show();
                }
            }

            // No se pudo llegar al servidor (IP incorrecta, API apagada, sin red...).
            @Override
            public void onFailure(@NonNull Call<Medicamento> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnGuardar.setEnabled(true);
                Toast.makeText(MedicamentoFormActivity.this,
                        R.string.error_conexion, Toast.LENGTH_LONG).show();
            }
        });
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