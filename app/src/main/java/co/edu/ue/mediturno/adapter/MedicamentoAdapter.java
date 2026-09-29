package co.edu.ue.mediturno.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Medicamento;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MedicamentoAdapter
        extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private static final int STOCK_BAJO_LIMITE = 10;

    public interface OnMedicamentoListener {
        void onEditar(Medicamento medicamento);

        void onEliminar(Medicamento medicamento);
    }

    private final List<Medicamento> medicamentos;
    private final OnMedicamentoListener listener;

    public MedicamentoAdapter(List<Medicamento> medicamentos, OnMedicamentoListener listener) {
        this.medicamentos = medicamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = medicamentos.get(position);

        holder.tvNombre.setText(medicamento.getNombre());

        String descripcion = medicamento.getDescripcion();
        if (descripcion == null || descripcion.isEmpty()) {
            holder.tvDescripcion.setVisibility(View.GONE);
        } else {
            holder.tvDescripcion.setVisibility(View.VISIBLE);
            holder.tvDescripcion.setText(descripcion);
        }

        holder.tvCantidad.setText(holder.itemView.getContext()
                .getString(R.string.item_cantidad, medicamento.getCantidad()));
        holder.tvVencimiento.setText(holder.itemView.getContext()
                .getString(R.string.item_vencimiento, medicamento.getFechaVencimiento()));

        boolean stockBajo = medicamento.getCantidad() <= STOCK_BAJO_LIMITE;
        holder.tvStockBajo.setVisibility(stockBajo ? View.VISIBLE : View.GONE);

        holder.btnEditar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEditar(medicamentos.get(posicion));
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            int posicion = holder.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                listener.onEliminar(medicamentos.get(posicion));
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicamentos.size();
    }

    static class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        final TextView tvNombre;
        final TextView tvStockBajo;
        final TextView tvDescripcion;
        final TextView tvCantidad;
        final TextView tvVencimiento;
        final MaterialButton btnEditar;
        final MaterialButton btnEliminar;

        MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvStockBajo = itemView.findViewById(R.id.tvStockBajo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvVencimiento = itemView.findViewById(R.id.tvVencimiento);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}