package co.edu.ue.mediturno.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Cita;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    public interface OnCitaListener {
        void onReprogramar(Cita cita);

        void onCancelar(Cita cita);
    }

    private final List<Cita> citas;
    private final OnCitaListener listener;

    public CitaAdapter(List<Cita> citas, OnCitaListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citas.get(position);
        Context contexto = holder.itemView.getContext();

        holder.tvPaciente.setText(cita.getPaciente());
        holder.tvFechaHora.setText(contexto.getString(R.string.item_fecha_hora,
                cita.getFecha(), cita.getHora()));
        holder.tvMedico.setText(contexto.getString(R.string.item_medico, cita.getMedico()));
        holder.tvMotivo.setText(contexto.getString(R.string.item_motivo, cita.getMotivo()));

        boolean cancelada = Cita.ESTADO_CANCELADA.equals(cita.getEstado());
        if (cancelada) {
            holder.chipEstado.setText(R.string.estado_cancelada);
            holder.chipEstado.setChipBackgroundColorResource(R.color.fondo);
            holder.chipEstado.setTextColor(ContextCompat.getColor(contexto, R.color.error));
            holder.llAcciones.setVisibility(View.GONE);
        } else {
            holder.chipEstado.setText(R.string.estado_programada);
            holder.chipEstado.setChipBackgroundColorResource(R.color.verde_claro);
            holder.chipEstado.setTextColor(
                    ContextCompat.getColor(contexto, R.color.verde_secundario));
            holder.llAcciones.setVisibility(View.VISIBLE);
        }

        holder.btnReprogramar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onReprogramar(citas.get(posicion));
            }
        });

        holder.btnCancelarCita.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onCancelar(citas.get(posicion));
            }
        });
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {

        final TextView tvPaciente;
        final TextView tvFechaHora;
        final TextView tvMedico;
        final TextView tvMotivo;
        final Chip chipEstado;
        final LinearLayout llAcciones;
        final MaterialButton btnReprogramar;
        final MaterialButton btnCancelarCita;

        CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvMedico = itemView.findViewById(R.id.tvMedico);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            llAcciones = itemView.findViewById(R.id.llAcciones);
            btnReprogramar = itemView.findViewById(R.id.btnReprogramar);
            btnCancelarCita = itemView.findViewById(R.id.btnCancelarCita);
        }
    }
}