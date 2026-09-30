package co.edu.ue.mediturno.ui.usuarios;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_DOCUMENTO = "documento";
    public static final String EXTRA_TELEFONO = "telefono";
    public static final String EXTRA_CORREO = "correo";
    public static final String EXTRA_ROL = "rol";

    // Segunda instancia de Firebase, usada solo para crear cuentas de otros usuarios
    // sin cerrar la sesión de quien está administrando.
    private static final String NOMBRE_APP_SECUNDARIA = "creacion_usuarios";

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
            // El correo es el que une al usuario con su cuenta de acceso, por eso no se edita.
            etCorreo.setEnabled(false);
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
        String correo = texto(etCorreo).toLowerCase(Locale.ROOT);
        String contrasena = etContrasena.getText() != null
                ? etContrasena.getText().toString() : "";
        String rol = actvRol.getText() != null ? actvRol.getText().toString().trim() : "";

        if (!validarCampos(nombre, documento, telefono, correo, contrasena, rol)) {
            return;
        }

        Usuario usuario = new Usuario(idUsuario, nombre, documento, telefono, correo, rol);
        btnGuardar.setEnabled(false);

        if (modoEdicion) {
            // PUT /api/usuarios/{id}
            enviarAlApi(ApiClient.getApiService().actualizarUsuario(idUsuario, usuario), null);
        } else {
            crearCuentaYPerfil(usuario, contrasena);
        }
    }

    // Crear un usuario nuevo: primero su cuenta de acceso en Firebase (correo y contraseña)
    // y luego su perfil con el rol en la API.
    private void crearCuentaYPerfil(Usuario usuario, String contrasena) {
        FirebaseAuth authSecundaria = autenticacionSecundaria();

        authSecundaria.createUserWithEmailAndPassword(usuario.getCorreo(), contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // POST /api/usuarios
                        enviarAlApi(ApiClient.getApiService().crearUsuario(usuario), authSecundaria);
                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        // La cuenta de acceso ya existía (por ejemplo, alguien que se registró
                        // antes): solo se crea su perfil con el rol elegido.
                        Toast.makeText(this, R.string.cuenta_vinculada, Toast.LENGTH_LONG).show();
                        enviarAlApi(ApiClient.getApiService().crearUsuario(usuario), null);
                    } else {
                        btnGuardar.setEnabled(true);
                        String mensaje = task.getException() != null
                                ? task.getException().getLocalizedMessage()
                                : getString(R.string.error_conexion);
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // authNueva: sesión de la cuenta recién creada en Firebase (null si no se creó ninguna).
    private void enviarAlApi(Call<Usuario> llamada, FirebaseAuth authNueva) {
        llamada.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call,
                                   @NonNull Response<Usuario> response) {
                if (response.isSuccessful()) {
                    cerrarSesionSecundaria(authNueva);
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    setResult(RESULT_OK);
                    Toast.makeText(UsuarioFormActivity.this, R.string.usuario_guardado,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    revertirCuenta(authNueva);
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    btnGuardar.setEnabled(true);
                    Toast.makeText(UsuarioFormActivity.this,
                            ApiErrores.mensaje(UsuarioFormActivity.this, response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                revertirCuenta(authNueva);
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnGuardar.setEnabled(true);
                Toast.makeText(UsuarioFormActivity.this, R.string.error_conexion,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private FirebaseAuth autenticacionSecundaria() {
        FirebaseApp secundaria;
        try {
            secundaria = FirebaseApp.getInstance(NOMBRE_APP_SECUNDARIA);
        } catch (IllegalStateException e) {
            secundaria = FirebaseApp.initializeApp(getApplicationContext(),
                    FirebaseApp.getInstance().getOptions(), NOMBRE_APP_SECUNDARIA);
        }
        return FirebaseAuth.getInstance(secundaria);
    }

    private void cerrarSesionSecundaria(FirebaseAuth authNueva) {
        if (authNueva != null) {
            authNueva.signOut();
        }
    }

    // Si la API no pudo guardar el perfil, se borra la cuenta recién creada en Firebase
    // para no dejarla a medias.
    private void revertirCuenta(FirebaseAuth authNueva) {
        if (authNueva == null) {
            return;
        }
        FirebaseUser user = authNueva.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(tarea -> authNueva.signOut());
        } else {
            authNueva.signOut();
        }
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