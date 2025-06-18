package com.example.lab6_20212591;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovimientoAdapter extends RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder> {

    private List<Movimiento> movimientosList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MovimientoAdapter(List<Movimiento> movimientosList) {
        this.movimientosList = movimientosList;
    }

    public void setMovimientosList(List<Movimiento> movimientosList) {
        this.movimientosList = movimientosList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        Movimiento movimiento = movimientosList.get(position);
        holder.bind(movimiento);
    }

    @Override
    public int getItemCount() {
        return movimientosList.size();
    }

    public class MovimientoViewHolder extends RecyclerView.ViewHolder {

        TextView tvTipoTarjeta, tvIdTarjeta, tvFechaMovimiento, tvRuta, tvTiempoViaje;

        public MovimientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoTarjeta = itemView.findViewById(R.id.tv_tipo_tarjeta);
            tvIdTarjeta = itemView.findViewById(R.id.tv_id_tarjeta);
            tvFechaMovimiento = itemView.findViewById(R.id.tv_fecha_movimiento);
            tvRuta = itemView.findViewById(R.id.tv_ruta);
            tvTiempoViaje = itemView.findViewById(R.id.tv_tiempo_viaje);
        }

        public void bind(Movimiento movimiento) {
            // Ya no hay sistemaTransporte, solo tipoTarjeta
            tvTipoTarjeta.setText("Tipo: " + movimiento.getTipoTarjeta());

            tvIdTarjeta.setText("ID Tarjeta: " + (movimiento.getId() != null ? movimiento.getId() : "-"));
            tvFechaMovimiento.setText("Fecha: " + dateFormat.format(movimiento.getFechaMovimiento()));
            tvRuta.setText("Ruta: " + movimiento.getEstacionEntrada() + " → " + movimiento.getEstacionSalida());

            // Mostrar tiempo de viaje solo si es Línea 1
            if (movimiento.getTipoTarjeta().equalsIgnoreCase("Linea1")) {
                long tiempoMinutos = movimiento.getTiempoViajeMillis() / (60 * 1000);
                tvTiempoViaje.setText("Duración: " + tiempoMinutos + " min");
                tvTiempoViaje.setVisibility(View.VISIBLE);
            } else {
                tvTiempoViaje.setVisibility(View.GONE);
            }
        }
    }
}