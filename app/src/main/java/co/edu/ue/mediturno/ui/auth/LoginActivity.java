package co.edu.ue.mediturno.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.ue.mediturno.MainActivity;
import co.edu.ue.mediturno.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

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
        mAuth = FirebaseAuth.getInstance(); // Inicializar Firebase Auth

        // Verificar si el usuario ya está autenticado
        if (mAuth.getCurrentUser() != null) {
            irAPantallaPrincipal("PACIENTE");
            return;
        }

        inicializarVistas();
        configurarEventos();
    }

    private void inicializarVistas() {
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvRegistro = findViewById(R.id.tvRegistro);
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
        // Iniciar sesión con Firebase Auth
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Autenticación exitosa
                        irAPantallaPrincipal("PACIENTE");
                    } else {
                        String mensajeError = task.getException() != null ?
                                task.getException().getLocalizedMessage() : "Credenciales inválidas";
                        Toast.makeText(LoginActivity.this, "Error de inicio de sesión: " + mensajeError, Toast.LENGTH_LONG).show();
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

    private void irAPantallaPrincipal(String rol) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("rol", rol);
        startActivity(intent);
        finish();
    }
}