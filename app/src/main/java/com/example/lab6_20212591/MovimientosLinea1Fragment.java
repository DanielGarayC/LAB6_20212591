package com.example.lab6_20212591;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.lab6_20212591.R;
import com.example.lab6_20212591.MovimientoAdapter;
import com.example.lab6_20212591.Movimiento;

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

    private TextInputEditText etIdTarjeta, etFechaMovimiento, etEstacionEntrada, etEstacionSalida, etTiempoViaje;
    private MaterialButton btnGuardarMovimiento;
    private RecyclerView recyclerViewMovimientos;
    private MovimientoAdapter movimientoAdapter;
    private List<Movimiento> movimientosList;
    private TextView tvNoMovimientos;

    private Calendar calendar = Calendar.getInstance();

    public MovimientosLinea1Fragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movimientos_linea1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIdTarjeta = view.findViewById(R.id.et_id_tarjeta_linea1);
        etFechaMovimiento = view.findViewById(R.id.et_fecha_movimiento_linea1);
        etEstacionEntrada = view.findViewById(R.id.et_estacion_entrada_linea1);
        etEstacionSalida = view.findViewById(R.id.et_estacion_salida_linea1);
        etTiempoViaje = view.findViewById(R.id.et_tiempo_viaje_linea1);
        btnGuardarMovimiento = view.findViewById(R.id.btn_guardar_movimiento_linea1);
        recyclerViewMovimientos = view.findViewById(R.id.recyclerViewMovimientosLinea1);
        tvNoMovimientos = view.findViewById(R.id.tv_no_movimientos_linea1);

        etFechaMovimiento.setOnClickListener(v -> showDatePicker());
        updateDateField(calendar.getTime());

        btnGuardarMovimiento.setOnClickListener(v -> guardarMovimiento());

        movimientosList = new ArrayList<>();
        movimientoAdapter = new MovimientoAdapter(movimientosList);
        recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMovimientos.setAdapter(movimientoAdapter);

        cargarMovimientosLinea1();
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField(calendar.getTime());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField(Date date) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        etFechaMovimiento.setText(sdf.format(date));
    }

    private void guardarMovimiento() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Debe iniciar sesión para guardar movimientos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String idTarjeta = etIdTarjeta.getText().toString().trim();
        Date fechaMovimiento = calendar.getTime();
        String estacionEntrada = etEstacionEntrada.getText().toString().trim();
        String estacionSalida = etEstacionSalida.getText().toString().trim();
        String tiempoViajeStr = etTiempoViaje.getText().toString().trim();

        if (idTarjeta.isEmpty() || estacionEntrada.isEmpty() || estacionSalida.isEmpty() || tiempoViajeStr.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos para Línea 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        long tiempoViajeMillis = Long.parseLong(tiempoViajeStr) * 60 * 1000;
        // Usamos 'idTarjeta' para el campo 'id' del Movimiento para que el Adapter pueda mostrarlo.
        Movimiento nuevoMovimiento = new Movimiento(userId, idTarjeta, fechaMovimiento, estacionEntrada, estacionSalida, tiempoViajeMillis);

        db.collection("movimientos")
                .add(nuevoMovimiento)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Movimiento Línea 1 guardado con ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Movimiento Línea 1 guardado exitosamente!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    cargarMovimientosLinea1(); // Recargar la lista
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar movimiento Línea 1", e);
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearFields() {
        etIdTarjeta.setText("");
        etFechaMovimiento.setText("");
        etEstacionEntrada.setText("");
        etEstacionSalida.setText("");
        etTiempoViaje.setText("");
        updateDateField(Calendar.getInstance().getTime());
    }

    private void cargarMovimientosLinea1() {
        if (currentUser == null) {
            tvNoMovimientos.setVisibility(View.VISIBLE);
            movimientosList.clear();
            movimientoAdapter.notifyDataSetChanged();
            return;
        }

        db.collection("movimientos")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("tipoTarjeta", "Linea1")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movimientosList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movimiento movimiento = document.toObject(Movimiento.class);
                            movimiento.setId(document.getId()); // Asigna el ID del documento de Firestore a tu objeto Movimiento
                            movimientosList.add(movimiento);
                        }
                        movimientoAdapter.setMovimientosList(movimientosList);
                        tvNoMovimientos.setVisibility(movimientosList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e(TAG, "Error al cargar movimientos de Línea 1: ", task.getException());
                        Toast.makeText(getContext(), "Error al cargar movimientos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        tvNoMovimientos.setVisibility(View.VISIBLE);
                    }
                });
    }
}