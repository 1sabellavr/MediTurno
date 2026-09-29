package co.edu.ue.mediturno.adapter;

import android.content.Context;
import android.location.Location;
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
import co.edu.ue.mediturno.model.PuntoAtencion;
import co.edu.ue.mediturno.util.UbicacionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    public interface OnCitaListener {
        void onReprogramar(Cita cita);

        void onCancelar(Cita cita);

        void onVerMapa(Cita cita);

        void onComoLlegar(Cita cita);
    }

    private final List<Cita> citas;
    private final OnCitaListener listener;
    private Location ubicacion;

    public CitaAdapter(List<Cita> citas, OnCitaListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    // Ubicación del GPS del dispositivo, para mostrar la distancia a cada punto de atención.
    public void setUbicacion(Location ubicacion) {
        this.ubicacion = ubicacion;
        notifyDataSetChanged();
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

        PuntoAtencion punto = cita.getPuntoAtencion();
        if (punto != null) {
            holder.tvPunto.setVisibility(View.VISIBLE);
            holder.tvPunto.setText(contexto.getString(R.string.item_punto,
                    punto.getNombre(), punto.getDireccion()));
            holder.llMapa.setVisibility(View.VISIBLE);

            if (ubicacion != null) {
                float metros = UbicacionHelper.distanciaMetros(ubicacion, punto);
                holder.tvDistancia.setVisibility(View.VISIBLE);
                holder.tvDistancia.setText(contexto.getString(R.string.distancia_a_ti,
                        UbicacionHelper.formatearDistancia(metros)));
            } else {
                holder.tvDistancia.setVisibility(View.GONE);
            }
        } else {
            holder.tvPunto.setVisibility(View.GONE);
            holder.tvDistancia.setVisibility(View.GONE);
            holder.llMapa.setVisibility(View.GONE);
        }

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

        holder.btnVerMapa.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onVerMapa(citas.get(posicion));
            }
        });

        holder.btnComoLlegar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onComoLlegar(citas.get(posicion));
            }
        });

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
        final TextView tvPunto;
        final TextView tvDistancia;
        final TextView tvMotivo;
        final Chip chipEstado;
        final LinearLayout llMapa;
        final LinearLayout llAcciones;
        final MaterialButton btnVerMapa;
        final MaterialButton btnComoLlegar;
        final MaterialButton btnReprogramar;
        final MaterialButton btnCancelarCita;

        CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvMedico = itemView.findViewById(R.id.tvMedico);
            tvPunto = itemView.findViewById(R.id.tvPunto);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            llMapa = itemView.findViewById(R.id.llMapa);
            llAcciones = itemView.findViewById(R.id.llAcciones);
            btnVerMapa = itemView.findViewById(R.id.btnVerMapa);
            btnComoLlegar = itemView.findViewById(R.id.btnComoLlegar);
            btnReprogramar = itemView.findViewById(R.id.btnReprogramar);
            btnCancelarCita = itemView.findViewById(R.id.btnCancelarCita);
        }
    }
}