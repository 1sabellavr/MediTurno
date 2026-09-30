package co.edu.ue.mediturno.ui.auth;

import android.content.Intent;
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
import co.edu.ue.mediturno.util.DataStoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Datos de la sesión que se envían a la pantalla principal (y de ahí a Citas).
    public static final String EXTRA_USUARIO_ID = "usuarioId";
    public static final String EXTRA_USUARIO_NOMBRE = "usuarioNombre";

    private TextInputLayout tilCorreo;
    private TextInputLayout tilContrasena;
    private TextInputEditText etCorreo;
    private TextInputEditText etContrasena;
    private MaterialButton btnIngresar;
    private TextView tvRegistro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        inicializarVistas();
        configurarEventos();
        prellenarUltimoCorreo();

        // Si ya hay una sesión iniciada, se consulta el rol y se entra directo.
        FirebaseUser actual = mAuth.getCurrentUser();
        if (actual != null && actual.getEmail() != null) {
            consultarRolYEntrar(actual.getEmail());
        }
    }

    private void inicializarVistas() {
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvRegistro = findViewById(R.id.tvRegistro);
    }

    // DataStore: si ya se inició sesión antes, se deja escrito el último correo usado.
    private void prellenarUltimoCorreo() {
        DataStoreHelper.obtenerUltimoCorreo(this)
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(correo -> {
                    boolean campoVacio = etCorreo.getText() == null
                            || etCorreo.getText().length() == 0;
                    if (!correo.isEmpty() && campoVacio) {
                        etCorreo.setText(correo);
                    }
                }, error -> {
                    // Si no se puede leer, el campo queda vacío.
                });
    }

    private void configurarEventos() {
        btnIngresar.setOnClickListener(v -> intentarLogin());

        tvRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        String correo = etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
        String contrasena = etContrasena.getText() != null ? etContrasena.getText().toString() : "";

        if (!validarCampos(correo, contrasena)) {
            return;
        }

        btnIngresar.setEnabled(false);

        // 1. Iniciar sesión con Firebase Auth (correo y contraseña)
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // DataStore: se recuerda el correo para la próxima vez
                        DataStoreHelper.guardarUltimoCorreo(getApplicationContext(), correo);
                        // 2. Autenticación exitosa: se pide el rol a la API
                        consultarRolYEntrar(correo);
                    } else {
                        btnIngresar.setEnabled(true);
                        String mensajeError = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Credenciales inválidas";
                        Toast.makeText(LoginActivity.this, "Error de inicio de sesión: " + mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // GET /api/usuarios/por-correo?correo=...
    private void consultarRolYEntrar(String correo) {
        btnIngresar.setEnabled(false);

        ApiClient.getApiService().obtenerUsuarioPorCorreo(correo).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    irAPantallaPrincipal(response.body());
                } else if (response.code() == 404) {
                    // La cuenta existe en Firebase pero no tiene perfil en el sistema.
                    mAuth.signOut();
                    btnIngresar.setEnabled(true);
                    Toast.makeText(LoginActivity.this, R.string.sin_perfil, Toast.LENGTH_LONG).show();
                } else {
                    btnIngresar.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            ApiErrores.mensaje(LoginActivity.this, response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                btnIngresar.setEnabled(true);
                Toast.makeText(LoginActivity.this, R.string.error_conexion, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validarCampos(String correo, String contrasena) {
        boolean valido = true;

        tilCorreo.setError(null);
        tilContrasena.setError(null);

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

        return valido;
    }

    private void irAPantallaPrincipal(Usuario usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol() : MainActivity.ROL_PACIENTE;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("rol", rol);
        intent.putExtra(EXTRA_USUARIO_ID, usuario.getId());
        intent.putExtra(EXTRA_USUARIO_NOMBRE, usuario.getNombre());
        startActivity(intent);
        finish();
    }
}