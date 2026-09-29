package co.edu.ue.mediturno.ui.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UsuarioFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_DOCUMENTO = "documento";
    public static final String EXTRA_TELEFONO = "telefono";
    public static final String EXTRA_CORREO = "correo";
    public static final String EXTRA_ROL = "rol";

    private TextView tvTituloForm;
    private TextInputLayout tilNombre;
    private TextInputLayout tilDocumento;
    private TextInputLayout tilTelefono;
    private TextInputLayout tilCorreo;
    private TextInputLayout tilContrasena;
    private TextInputLayout tilRol;
    private TextInputEditText etNombre;
    private TextInputEditText etDocumento;
    private TextInputEditText etTelefono;
    private TextInputEditText etCorreo;
    private TextInputEditText etContrasena;
    private MaterialAutoCompleteTextView actvRol;
    private MaterialButton btnCancelar;
    private MaterialButton btnGuardar;

    private int idUsuario;
    private boolean modoEdicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_form);

        inicializarVistas();
        configurarRoles();
        cargarDatosSiEsEdicion();
        configurarEventos();
    }

    private void inicializarVistas() {
        tvTituloForm = findViewById(R.id.tvTituloForm);
        tilNombre = findViewById(R.id.tilNombre);
        tilDocumento = findViewById(R.id.tilDocumento);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilRol = findViewById(R.id.tilRol);
        etNombre = findViewById(R.id.etNombre);
        etDocumento = findViewById(R.id.etDocumento);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        actvRol = findViewById(R.id.actvRol);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
    }

    private void configurarRoles() {
        // TODO-API: GET /roles
        // Recibe: lista de roles disponibles. Reemplazar el arreglo "roles" de strings.xml
        // por la respuesta de la API.
        String[] roles = getResources().getStringArray(R.array.roles);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, roles);
        actvRol.setAdapter(adaptador);
    }

    private void cargarDatosSiEsEdicion() {
        Intent intent = getIntent();
        idUsuario = intent.getIntExtra(EXTRA_ID, 0);
        modoEdicion = idUsuario != 0;

        if (modoEdicion) {
            tvTituloForm.setText(R.string.usuario_form_titulo_editar);
            etNombre.setText(intent.getStringExtra(EXTRA_NOMBRE));
            etDocumento.setText(intent.getStringExtra(EXTRA_DOCUMENTO));
            etTelefono.setText(intent.getStringExtra(EXTRA_TELEFONO));
            etCorreo.setText(intent.getStringExtra(EXTRA_CORREO));
            actvRol.setText(intent.getStringExtra(EXTRA_ROL), false);
            // La contraseña solo se pide al crear un usuario.
            tilContrasena.setVisibility(View.GONE);
        } else {
            tvTituloForm.setText(R.string.usuario_form_titulo_crear);
        }
    }

    private void configurarEventos() {
        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void guardar() {
        String nombre = texto(etNombre);
        String documento = texto(etDocumento);
        String telefono = texto(etTelefono);
        String correo = texto(etCorreo);
        String contrasena = etContrasena.getText() != null
                ? etContrasena.getText().toString() : "";
        String rol = actvRol.getText() != null ? actvRol.getText().toString().trim() : "";

        if (!validarCampos(nombre, documento, telefono, correo, contrasena, rol)) {
            return;
        }

        // TODO-API: si modoEdicion es false -> POST /usuarios (envía nombre, documento,
        // teléfono, correo, contraseña y rol). Si es true -> PUT /usuarios/{id}
        // (envía nombre, documento, teléfono, correo y rol).
        // Reemplazar el resultado de prueba de abajo por la respuesta real de la API
        // y mostrar el error si el correo o el documento ya existen.
        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_ID, idUsuario);
        resultado.putExtra(EXTRA_NOMBRE, nombre);
        resultado.putExtra(EXTRA_DOCUMENTO, documento);
        resultado.putExtra(EXTRA_TELEFONO, telefono);
        resultado.putExtra(EXTRA_CORREO, correo);
        resultado.putExtra(EXTRA_ROL, rol);
        setResult(RESULT_OK, resultado);

        Toast.makeText(this, R.string.usuario_guardado, Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validarCampos(String nombre, String documento, String telefono,
                                  String correo, String contrasena, String rol) {
        boolean valido = true;

        tilNombre.setError(null);
        tilDocumento.setError(null);
        tilTelefono.setError(null);
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        tilRol.setError(null);

        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.error_nombre_vacio));
            valido = false;
        }

        if (documento.isEmpty()) {
            tilDocumento.setError(getString(R.string.error_documento_vacio));
            valido = false;
        } else if (!documento.matches("\\d{6,12}")) {
            tilDocumento.setError(getString(R.string.error_documento_invalido));
            valido = false;
        }

        if (telefono.isEmpty()) {
            tilTelefono.setError(getString(R.string.error_telefono_vacio));
            valido = false;
        } else if (!telefono.matches("\\d{10}")) {
            tilTelefono.setError(getString(R.string.error_telefono_invalido));
            valido = false;
        }

        if (correo.isEmpty()) {
            tilCorreo.setError(getString(R.string.error_correo_vacio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.error_correo_invalido));
            valido = false;
        }

        if (!modoEdicion) {
            if (contrasena.isEmpty()) {
                tilContrasena.setError(getString(R.string.error_contrasena_vacia));
                valido = false;
            } else if (contrasena.length() < 6) {
                tilContrasena.setError(getString(R.string.error_contrasena_corta));
                valido = false;
            }
        }

        if (rol.isEmpty()) {
            tilRol.setError(getString(R.string.error_rol_vacio));
            valido = false;
        }

        return valido;
    }
}