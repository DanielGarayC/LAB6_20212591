package com.example.lab6_20212591;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovimientosLimaPassFragment extends Fragment {

    private static final String TAG = "MovimientosLimaPass";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextInputEditText etIdTarjeta, etFechaMovimiento, etParaderoEntrada, etParaderoSalida;
    private MaterialButton btnGuardarMovimiento;
    private RecyclerView recyclerViewMovimientos;
    private MovimientoAdapter movimientoAdapter;
    private List<Movimiento> movimientosList;
    private TextView tvNoMovimientos;

    private Calendar calendar = Calendar.getInstance();

    public MovimientosLimaPassFragment() { }

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
        return inflater.inflate(R.layout.fragment_movimientos_lima_pass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etIdTarjeta = view.findViewById(R.id.et_id_tarjeta_limapass);
        etFechaMovimiento = view.findViewById(R.id.et_fecha_movimiento_limapass);
        etParaderoEntrada = view.findViewById(R.id.et_paradero_entrada_limapass);
        etParaderoSalida = view.findViewById(R.id.et_paradero_salida_limapass);
        btnGuardarMovimiento = view.findViewById(R.id.btn_guardar_movimiento_limapass);
        recyclerViewMovimientos = view.findViewById(R.id.recyclerViewMovimientosLimaPass);
        tvNoMovimientos = view.findViewById(R.id.tv_no_movimientos_limapass);

        etFechaMovimiento.setOnClickListener(v -> showDatePicker());
        updateDateField(calendar.getTime());

        btnGuardarMovimiento.setOnClickListener(v -> guardarMovimiento());

        movimientosList = new ArrayList<>();
        movimientoAdapter = new MovimientoAdapter(movimientosList);
        recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMovimientos.setAdapter(movimientoAdapter);

        cargarMovimientosLimaPass();
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
        String paraderoEntrada = etParaderoEntrada.getText().toString().trim();
        String paraderoSalida = etParaderoSalida.getText().toString().trim();

        if (idTarjeta.isEmpty() || paraderoEntrada.isEmpty() || paraderoSalida.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos para Lima Pass.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Usamos 'idTarjeta' para el campo 'id' del Movimiento para que el Adapter pueda mostrarlo.
        Movimiento nuevoMovimiento = new Movimiento(userId, idTarjeta, fechaMovimiento, paraderoEntrada, paraderoSalida);

        db.collection("movimientos")
                .add(nuevoMovimiento)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Movimiento Lima Pass guardado con ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Movimiento Lima Pass guardado exitosamente!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    cargarMovimientosLimaPass(); // Recargar la lista
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar movimiento Lima Pass", e);
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearFields() {
        etIdTarjeta.setText("");
        etFechaMovimiento.setText("");
        etParaderoEntrada.setText("");
        etParaderoSalida.setText("");
        updateDateField(Calendar.getInstance().getTime());
    }

    private void cargarMovimientosLimaPass() {
        if (currentUser == null) {
            tvNoMovimientos.setVisibility(View.VISIBLE);
            movimientosList.clear();
            movimientoAdapter.notifyDataSetChanged();
            return;
        }

        db.collection("movimientos")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("tipoTarjeta", "LimaPass")
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
                        tvNoMovimientos.setVisibility(movimientosList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e(TAG, "Error al cargar movimientos de Lima Pass: ", task.getException());
                        Toast.makeText(getContext(), "Error al cargar movimientos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        tvNoMovimientos.setVisibility(View.VISIBLE);
                    }
                });
    }
}