package co.edu.ue.mediturno.ui.tratamiento;

import android.app.Activity;
import android.content.Intent;
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
import co.edu.ue.mediturno.adapter.TratamientoAdapter;
import co.edu.ue.mediturno.model.Tratamiento;
import co.edu.ue.mediturno.repository.TratamientoRepository;
import co.edu.ue.mediturno.util.ArchivoHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// "Mi tratamiento": CRUD local sobre SQLite. No usa la API; cada usuario ve solo lo suyo.
public class TratamientoFragment extends Fragment
        implements TratamientoAdapter.OnTratamientoListener {

    private final List<Tratamiento> tratamientos = new ArrayList<>();
    private TratamientoAdapter adapter;
    private TextView tvVacio;
    private TratamientoRepository repositorio;
    private String usuario = "";
    private ActivityResultLauncher<Intent> formLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repositorio = new TratamientoRepository(requireContext());

        FirebaseUser actual = FirebaseAuth.getInstance().getCurrentUser();
        usuario = actual != null && actual.getEmail() != null ? actual.getEmail() : "";

        // Cuando el formulario guarda, se vuelve a leer la lista de SQLite.
        formLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarTratamientos();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_tratamiento, container, false);

        RecyclerView rvTratamientos = vista.findViewById(R.id.rvTratamientos);
        tvVacio = vista.findViewById(R.id.tvVacio);
        ExtendedFloatingActionButton fabNuevo = vista.findViewById(R.id.fabNuevoTratamiento);

        adapter = new TratamientoAdapter(tratamientos, this);
        rvTratamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTratamientos.setAdapter(adapter);

        fabNuevo.setOnClickListener(v -> formLauncher.launch(
                new Intent(requireContext(), TratamientoFormActivity.class)));

        MaterialButton btnExportar = vista.findViewById(R.id.btnExportar);
        btnExportar.setOnClickListener(v -> exportarTratamiento());

        cargarTratamientos();
        return vista;
    }

    // READ: SELECT de SQLite
    private void cargarTratamientos() {
        tratamientos.clear();
        tratamientos.addAll(repositorio.obtenerPorUsuario(usuario));
        adapter.notifyDataSetChanged();
        tvVacio.setVisibility(tratamientos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ARCHIVOS: se guarda el tratamiento en un archivo de texto de la memoria del teléfono,
    // se vuelve a leer el archivo y se ofrece compartirlo.
    private void exportarTratamiento() {
        if (tratamientos.isEmpty()) {
            Toast.makeText(requireContext(), R.string.tratamiento_vacio,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(new Date());

        StringBuilder contenido = new StringBuilder();
        contenido.append(getString(R.string.exportar_encabezado)).append("\n");
        contenido.append(getString(R.string.exportar_usuario, usuario)).append("\n");
        contenido.append(getString(R.string.exportar_fecha, fecha)).append("\n\n");

        int numero = 1;
        for (Tratamiento tratamiento : tratamientos) {
            contenido.append(numero++).append(". ")
                    .append(tratamiento.getMedicamento()).append(" | ")
                    .append(getString(R.string.item_dosis, tratamiento.getDosis())).append(" | ")
                    .append(getString(R.string.item_hora_toma, tratamiento.getHora()))
                    .append("\n");
            String notas = tratamiento.getNotas();
            if (notas != null && !notas.isEmpty()) {
                contenido.append("   ").append(notas).append("\n");
            }
        }

        String nombreArchivo = usuario.isEmpty()
                ? "tratamiento.txt"
                : "tratamiento_" + usuario.replaceAll("[^A-Za-z0-9]", "_") + ".txt";

        // Escribir el archivo
        boolean guardado = ArchivoHelper.guardarComprobante(
                requireContext(), nombreArchivo, contenido.toString());
        if (!guardado) {
            Toast.makeText(requireContext(), R.string.error_exportar,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Leer el archivo guardado
        String leido = ArchivoHelper.leerComprobante(requireContext(), nombreArchivo);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.exportar_titulo)
                .setMessage(leido)
                .setNegativeButton(R.string.btn_cerrar, null)
                .setPositiveButton(R.string.btn_compartir, (dialog, which) -> {
                    Intent enviar = new Intent(Intent.ACTION_SEND);
                    enviar.setType("text/plain");
                    enviar.putExtra(Intent.EXTRA_TEXT, leido);
                    startActivity(Intent.createChooser(enviar,
                            getString(R.string.compartir_titulo)));
                })
                .show();
    }

    @Override
    public void onEditar(Tratamiento tratamiento) {
        Intent intent = new Intent(requireContext(), TratamientoFormActivity.class);
        intent.putExtra(TratamientoFormActivity.EXTRA_ID, tratamiento.getId());
        intent.putExtra(TratamientoFormActivity.EXTRA_MEDICAMENTO, tratamiento.getMedicamento());
        intent.putExtra(TratamientoFormActivity.EXTRA_DOSIS, tratamiento.getDosis());
        intent.putExtra(TratamientoFormActivity.EXTRA_HORA, tratamiento.getHora());
        intent.putExtra(TratamientoFormActivity.EXTRA_NOTAS, tratamiento.getNotas());
        formLauncher.launch(intent);
    }

    @Override
    public void onEliminar(Tratamiento tratamiento) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_tratamiento_titulo)
                .setMessage(getString(R.string.eliminar_tratamiento_mensaje,
                        tratamiento.getMedicamento()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) -> {
                    // DELETE en SQLite
                    if (repositorio.eliminar(tratamiento.getId()) > 0) {
                        cargarTratamientos();
                        Toast.makeText(requireContext(), R.string.tratamiento_eliminado,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), R.string.error_bd_local,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }
}