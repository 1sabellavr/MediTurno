package co.edu.ue.mediturno.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Tratamiento;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TratamientoAdapter
        extends RecyclerView.Adapter<TratamientoAdapter.TratamientoViewHolder> {

    public interface OnTratamientoListener {
        void onEditar(Tratamiento tratamiento);

        void onEliminar(Tratamiento tratamiento);
    }

    private final List<Tratamiento> tratamientos;
    private final OnTratamientoListener listener;

    public TratamientoAdapter(List<Tratamiento> tratamientos, OnTratamientoListener listener) {
        this.tratamientos = tratamientos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TratamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tratamiento, parent, false);
        return new TratamientoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TratamientoViewHolder holder, int position) {
        Tratamiento tratamiento = tratamientos.get(position);
        Context contexto = holder.itemView.getContext();

        holder.tvMedicamento.setText(tratamiento.getMedicamento());
        holder.tvDosis.setText(contexto.getString(R.string.item_dosis, tratamiento.getDosis()));
        holder.tvHora.setText(contexto.getString(R.string.item_hora_toma, tratamiento.getHora()));

        String notas = tratamiento.getNotas();
        if (notas == null || notas.isEmpty()) {
            holder.tvNotas.setVisibility(View.GONE);
        } else {
            holder.tvNotas.setVisibility(View.VISIBLE);
            holder.tvNotas.setText(notas);
        }

        holder.btnEditar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEditar(tratamientos.get(posicion));
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEliminar(tratamientos.get(posicion));
            }
        });
    }

    @Override
    public int getItemCount() {
        return tratamientos.size();
    }

    static class TratamientoViewHolder extends RecyclerView.ViewHolder {

        final TextView tvMedicamento;
        final TextView tvDosis;
        final TextView tvHora;
        final TextView tvNotas;
        final MaterialButton btnEditar;
        final MaterialButton btnEliminar;

        TratamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicamento = itemView.findViewById(R.id.tvMedicamento);
            tvDosis = itemView.findViewById(R.id.tvDosis);
            tvHora = itemView.findViewById(R.id.tvHoraToma);
            tvNotas = itemView.findViewById(R.id.tvNotas);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}