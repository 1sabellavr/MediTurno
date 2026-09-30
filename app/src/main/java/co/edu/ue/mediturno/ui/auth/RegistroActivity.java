package co.edu.ue.mediturno.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.MainActivity;
import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.api.ApiClient;
import co.edu.ue.mediturno.api.ApiErrores;
import co.edu.ue.mediturno.model.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilNombre;
    private TextInputLayout tilDocumento;
    private TextInputLayout tilTelefono;
    private TextInputLayout tilCorreo;
    private TextInputLayout tilContrasena;
    private TextInputLayout tilConfirmar;
    private TextInputEditText etNombre;
    private TextInputEditText etDocumento;
    private TextInputEditText etTelefono;
    private TextInputEditText etCorreo;
    private TextInputEditText etContrasena;
    private TextInputEditText etConfirmar;
    private MaterialButton btnRegistrar;
    private TextView tvVolverLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance(); //Inicializar Firebase Auth

        inicializarVistas();
        configurarEventos();
    }

    private void inicializarVistas() {
        tilNombre = findViewById(R.id.tilNombre);
        tilDocumento = findViewById(R.id.tilDocumento);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etDocumento = findViewById(R.id.etDocumento);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmar = findViewById(R.id.etConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);
    }

    private void configurarEventos() {
        btnRegistrar.setOnClickListener(v -> intentarRegistro());
        tvVolverLogin.setOnClickListener(v -> finish());
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void intentarRegistro() {
        String nombre = texto(etNombre);
        String documento = texto(etDocumento);
        String telefono = texto(etTelefono);
        String correo = texto(etCorreo).toLowerCase(Locale.ROOT);
        String contrasena = etContrasena.getText() != null ? etContrasena.getText().toString() : "";
        String confirmar = etConfirmar.getText() != null ? etConfirmar.getText().toString() : "";

        if (!validarCampos(nombre, documento, telefono, correo, contrasena, confirmar)) {
            return;
        }

        btnRegistrar.setEnabled(false);

        // 1. Crear la cuenta de acceso en Firebase Auth
        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Asignar el nombre al perfil del usuario en Firebase
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();
                            user.updateProfile(profileUpdates);
                        }

                        // 2. Guardar el perfil en la API con el rol por defecto (paciente)
                        crearPerfilEnApi(new Usuario(0, nombre, documento, telefono, correo,
                                MainActivity.ROL_PACIENTE));
                    } else {
                        btnRegistrar.setEnabled(true);
                        String mensajeError = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Error al registrar usuario";
                        Toast.makeText(RegistroActivity.this, "Error: " + mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // POST /api/usuarios
    private void crearPerfilEnApi(Usuario perfil) {
        ApiClient.getApiService().crearUsuario(perfil).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                if (response.isSuccessful()) {
                    // Se cierra la sesión para que el usuario inicie sesión desde el Login.
                    mAuth.signOut();
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    Toast.makeText(RegistroActivity.this, R.string.registro_exitoso, Toast.LENGTH_SHORT).show();
                    finish(); // Regresa a LoginActivity
                } else {
                    revertirCuenta();
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    Toast.makeText(RegistroActivity.this,
                            ApiErrores.mensaje(RegistroActivity.this, response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                revertirCuenta();
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(RegistroActivity.this, R.string.error_conexion, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Si la API no pudo guardar el perfil, se borra la cuenta recién creada en Firebase
    // para no dejarla a medias.
    private void revertirCuenta() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(tarea -> mAuth.signOut());
        } else {
            mAuth.signOut();
        }
        if (!isFinishing() && !isDestroyed()) {
            btnRegistrar.setEnabled(true);
        }
    }

    private boolean validarCampos(String nombre, String documento, String telefono,
                                  String correo, String contrasena, String confirmar) {
        boolean valido = true;

        tilNombre.setError(null);
        tilDocumento.setError(null);
        tilTelefono.setError(null);
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        tilConfirmar.setError(null);

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

        if (contrasena.isEmpty()) {
            tilContrasena.setError(getString(R.string.error_contrasena_vacia));
            valido = false;
        } else if (contrasena.length() < 6) {
            tilContrasena.setError(getString(R.string.error_contrasena_corta));
            valido = false;
        }

        if (confirmar.isEmpty()) {
            tilConfirmar.setError(getString(R.string.error_confirmar_vacio));
            valido = false;
        } else if (!confirmar.equals(contrasena)) {
            tilConfirmar.setError(getString(R.string.error_contrasenas_no_coinciden));
            valido = false;
        }

        return valido;
    }
}