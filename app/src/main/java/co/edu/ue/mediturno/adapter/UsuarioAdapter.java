package co.edu.ue.mediturno.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Usuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    public interface OnUsuarioListener {
        void onEditar(Usuario usuario);

        void onEliminar(Usuario usuario);
    }

    private final List<Usuario> usuarios;
    private final OnUsuarioListener listener;

    public UsuarioAdapter(List<Usuario> usuarios, OnUsuarioListener listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);

        holder.tvNombre.setText(usuario.getNombre());
        holder.tvCorreo.setText(usuario.getCorreo());
        holder.tvDocumento.setText(holder.itemView.getContext()
                .getString(R.string.item_documento, usuario.getDocumento()));
        holder.tvTelefono.setText(holder.itemView.getContext()
                .getString(R.string.item_telefono, usuario.getTelefono()));
        holder.chipRol.setText(usuario.getRol());

        holder.btnEditar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEditar(usuarios.get(posicion));
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEliminar(usuarios.get(posicion));
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        final TextView tvNombre;
        final TextView tvCorreo;
        final TextView tvDocumento;
        final TextView tvTelefono;
        final Chip chipRol;
        final MaterialButton btnEditar;
        final MaterialButton btnEliminar;

        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCorreo = itemView.findViewById(R.id.tvCorreo);
            tvDocumento = itemView.findViewById(R.id.tvDocumento);
            tvTelefono = itemView.findViewById(R.id.tvTelefono);
            chipRol = itemView.findViewById(R.id.chipRol);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}