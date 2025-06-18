package com.example.lab6_20212591;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.lab6_20212591.R;
import com.example.lab6_20212591.MovimientoAdapter;
import com.example.lab6_20212591.Movimiento;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovimientosLinea1Fragment extends Fragment {

    private static final String TAG = "MovimientosLinea1";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextInputEditText etIdTarjeta, etFechaMovimiento, etEstacionOrigen, etEstacionDestino, etTiempoViaje;
    private MaterialButton btnGuardarMovimiento;
    private RecyclerView recyclerViewMovimientos;
    private MovimientoAdapter movimientoAdapter;
    private List<Movimiento> movimientosList;
    private TextView tvNoMovimientos;
    private MaterialButton btnAplicarFiltro, btnLimpiarFiltro;
    private MaterialButton btnMostrarFormulario;
    private LinearLayout layoutRegistroMovimiento;
    private LinearLayout layoutFiltros;
    private TextInputEditText etFechaInicioFiltro, etFechaFinFiltro;
    private Calendar calendarInicioFiltro = Calendar.getInstance();
    private Calendar calendarFinFiltro = Calendar.getInstance();

    private Calendar calendarRegistro = Calendar.getInstance();

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MovimientosLinea1Fragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movimientos_linea1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIdTarjeta = view.findViewById(R.id.et_id_tarjeta_linea1);
        etFechaMovimiento = view.findViewById(R.id.et_fecha_movimiento_linea1);
        etEstacionOrigen = view.findViewById(R.id.et_estacion_origen_linea1);
        etEstacionDestino = view.findViewById(R.id.et_estacion_destino_linea1);
        etTiempoViaje = view.findViewById(R.id.et_tiempo_viaje_linea1);
        btnAplicarFiltro = view.findViewById(R.id.btn_aplicar_filtro_linea1);
        btnLimpiarFiltro = view.findViewById(R.id.btn_limpiar_filtro_linea1);
        btnGuardarMovimiento = view.findViewById(R.id.btn_guardar_movimiento_linea1);
        btnMostrarFormulario = view.findViewById(R.id.btn_mostrar_formulario_linea1);
        layoutRegistroMovimiento = view.findViewById(R.id.layout_registro_movimiento_linea1);
        layoutFiltros = view.findViewById(R.id.layout_filtros_linea1);

        recyclerViewMovimientos = view.findViewById(R.id.recyclerViewMovimientosLinea1);
        tvNoMovimientos = view.findViewById(R.id.tv_no_movimientos_linea1);

        etFechaInicioFiltro = view.findViewById(R.id.et_fecha_inicio_filtro_linea1);
        etFechaFinFiltro = view.findViewById(R.id.et_fecha_fin_filtro_linea1);

        etFechaInicioFiltro.setOnClickListener(v -> showDateTimePicker(etFechaInicioFiltro, calendarInicioFiltro));
        etFechaFinFiltro.setOnClickListener(v -> showDateTimePicker(etFechaFinFiltro, calendarFinFiltro));
        updateDateTimeField(etFechaInicioFiltro, calendarInicioFiltro.getTime());
        updateDateTimeField(etFechaFinFiltro, calendarFinFiltro.getTime());

        btnAplicarFiltro.setOnClickListener(v -> {
            Date fechaInicio = calendarInicioFiltro.getTime();
            Date fechaFin = calendarFinFiltro.getTime();

            if (fechaInicio.after(fechaFin)) {
                Toast.makeText(getContext(), "La fecha de inicio no puede ser mayor que la de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            filtrarMovimientosPorFecha(fechaInicio, fechaFin);
        });

        btnLimpiarFiltro.setOnClickListener(v -> {
            calendarInicioFiltro = Calendar.getInstance();
            calendarFinFiltro = Calendar.getInstance();
            updateDateTimeField(etFechaInicioFiltro, calendarInicioFiltro.getTime());
            updateDateTimeField(etFechaFinFiltro, calendarFinFiltro.getTime());
            cargarMovimientosLinea1();
        });

        layoutRegistroMovimiento.setVisibility(View.GONE);

        etFechaMovimiento.setOnClickListener(v -> showDateTimePicker(etFechaMovimiento, calendarRegistro));
        updateDateTimeField(etFechaMovimiento, calendarRegistro.getTime());

        btnGuardarMovimiento.setOnClickListener(v -> guardarMovimiento());

        btnMostrarFormulario.setOnClickListener(v -> {
            if (layoutRegistroMovimiento.getVisibility() == View.GONE) {
                layoutRegistroMovimiento.setVisibility(View.VISIBLE);
                btnMostrarFormulario.setText("Ocultar Formulario");
                layoutFiltros.setVisibility(View.GONE);
                recyclerViewMovimientos.setVisibility(View.GONE);
            } else {
                layoutRegistroMovimiento.setVisibility(View.GONE);
                btnMostrarFormulario.setText("Registrar Nuevo Movimiento Línea 1");
                layoutFiltros.setVisibility(View.VISIBLE);
                recyclerViewMovimientos.setVisibility(View.VISIBLE);
            }
            clearFields();
        });

        movimientosList = new ArrayList<>();
        movimientoAdapter = new MovimientoAdapter(movimientosList);
        recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMovimientos.setAdapter(movimientoAdapter);

        cargarMovimientosLinea1();
    }

    private void showDateTimePicker(TextInputEditText targetEditText, Calendar targetCalendar) {
        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            targetCalendar.set(Calendar.YEAR, year);
            targetCalendar.set(Calendar.MONTH, monthOfYear);
            targetCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                targetCalendar.set(Calendar.MINUTE, minute);
                updateDateTimeField(targetEditText, targetCalendar.getTime());
            }, targetCalendar.get(Calendar.HOUR_OF_DAY), targetCalendar.get(Calendar.MINUTE), false).show();

        }, targetCalendar.get(Calendar.YEAR), targetCalendar.get(Calendar.MONTH), targetCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateTimeField(TextInputEditText targetEditText, Date date) {
        targetEditText.setText(dateTimeFormat.format(date));
    }

    private void guardarMovimiento() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Debe iniciar sesión para guardar movimientos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String idTarjeta = etIdTarjeta.getText().toString().trim();
        Date fechaMovimiento = calendarRegistro.getTime();
        String estacionOrigen = etEstacionOrigen.getText().toString().trim();
        String estacionDestino = etEstacionDestino.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (idTarjeta.isEmpty() || estacionOrigen.isEmpty() || estacionDestino.isEmpty() || tiempoViajeStr.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos para Línea 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        long tiempoViajeMillis;
        try {
            tiempoViajeMillis = Long.parseLong(tiempoViajeStr) * 60 * 1000;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Tiempo de viaje debe ser un número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        Movimiento nuevoMovimiento = new Movimiento(currentUser.getUid(), idTarjeta, fechaMovimiento, estacionOrigen, estacionDestino, tiempoViajeMillis);
        nuevoMovimiento.setTipoTarjeta("Linea1");

        db.collection("usuarios").document(currentUser.getUid())
                .collection("movimientos")
                .add(nuevoMovimiento)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Movimiento guardado en subcolección: " + documentReference.getId());
                    Toast.makeText(getContext(), "Movimiento guardado exitosamente!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    layoutRegistroMovimiento.setVisibility(View.GONE);
                    layoutFiltros.setVisibility(View.VISIBLE);
                    recyclerViewMovimientos.setVisibility(View.VISIBLE);
                    btnMostrarFormulario.setText("Registrar Nuevo Movimiento Línea 1");
                    cargarMovimientosLinea1();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar movimiento", e);
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cargarMovimientosLinea1() {
        if (currentUser == null) return;

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("movimientos")
                .whereEqualTo("tipoTarjeta", "Linea1")
                .orderBy("fechaMovimiento", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movimientosList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movimiento movimiento = document.toObject(Movimiento.class);
                            movimiento.setId(document.getId());
                            movimientosList.add(movimiento);
                        }
                        movimientoAdapter.setMovimientosList(movimientosList);

                        if (movimientosList.isEmpty()) {
                            tvNoMovimientos.setVisibility(View.VISIBLE);
                            recyclerViewMovimientos.setVisibility(View.GONE);
                        } else {
                            tvNoMovimientos.setVisibility(View.GONE);
                            recyclerViewMovimientos.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error al cargar movimientos: ", task.getException());
                        Toast.makeText(getContext(), "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        etIdTarjeta.setText("");
        etEstacionOrigen.setText("");
        etEstacionDestino.setText("");
        etTiempoViaje.setText("");
        calendarRegistro = Calendar.getInstance();
        updateDateTimeField(etFechaMovimiento, calendarRegistro.getTime());
    }
    private void filtrarMovimientosPorFecha(Date inicio, Date fin) {
        if (currentUser == null) return;

        db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("movimientos")
                .whereEqualTo("tipoTarjeta", "Linea1")
                .whereGreaterThanOrEqualTo("fechaMovimiento", inicio)
                .whereLessThanOrEqualTo("fechaMovimiento", fin)
                .orderBy("fechaMovimiento", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movimientosList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movimiento movimiento = document.toObject(Movimiento.class);
                            movimiento.setId(document.getId());
                            movimientosList.add(movimiento);
                        }
                        movimientoAdapter.setMovimientosList(movimientosList);

                        if (movimientosList.isEmpty()) {
                            tvNoMovimientos.setVisibility(View.VISIBLE);
                            recyclerViewMovimientos.setVisibility(View.GONE);
                        } else {
                            tvNoMovimientos.setVisibility(View.GONE);
                            recyclerViewMovimientos.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error al filtrar movimientos: ", task.getException());
                        Toast.makeText(getContext(), "Error al filtrar movimientos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
