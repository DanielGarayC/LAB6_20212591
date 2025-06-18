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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MovimientosLimaPassFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextInputEditText etIdTarjeta, etFechaMovimiento, etParaderoEntrada, etParaderoSalida;
    private MaterialButton btnGuardarMovimiento;
    private RecyclerView recyclerViewMovimientos;
    private MovimientoAdapter movimientoAdapter;
    private List<Movimiento> movimientosList;
    private TextView tvNoMovimientos;
    private MaterialButton btnMostrarFormulario;
    private LinearLayout layoutRegistroMovimiento;
    private LinearLayout layoutFiltros;
    private TextInputEditText etFechaInicioFiltro, etFechaFinFiltro;
    private MaterialButton btnAplicarFiltro, btnLimpiarFiltro;

    private Calendar calendarRegistro = Calendar.getInstance();
    private Calendar calendarFiltroInicio = Calendar.getInstance();
    private Calendar calendarFiltroFin = Calendar.getInstance();
    private Date fechaFiltroInicioSeleccionada = null;
    private Date fechaFiltroFinSeleccionada = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public MovimientosLimaPassFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        btnMostrarFormulario = view.findViewById(R.id.btn_mostrar_formulario_limapass);
        layoutRegistroMovimiento = view.findViewById(R.id.layout_registro_movimiento_limapass);
        layoutFiltros = view.findViewById(R.id.layout_filtros_ordenamiento_limapass);
        etFechaInicioFiltro = view.findViewById(R.id.et_fecha_inicio_filtro_limapass);
        etFechaFinFiltro = view.findViewById(R.id.et_fecha_fin_filtro_limapass);
        btnAplicarFiltro = view.findViewById(R.id.btn_aplicar_filtro_limapass);
        btnLimpiarFiltro = view.findViewById(R.id.btn_limpiar_filtro_limapass);
        recyclerViewMovimientos = view.findViewById(R.id.recyclerViewMovimientosLimaPass);
        tvNoMovimientos = view.findViewById(R.id.tv_no_movimientos_limapass);

        layoutRegistroMovimiento.setVisibility(View.GONE);

        etFechaMovimiento.setOnClickListener(v -> showDatePicker(etFechaMovimiento, calendarRegistro));
        updateDateField(etFechaMovimiento, calendarRegistro.getTime());

        etFechaInicioFiltro.setOnClickListener(v -> showDatePicker(etFechaInicioFiltro, calendarFiltroInicio));
        etFechaFinFiltro.setOnClickListener(v -> showDatePicker(etFechaFinFiltro, calendarFiltroFin));

        btnGuardarMovimiento.setOnClickListener(v -> guardarMovimiento());

        btnMostrarFormulario.setOnClickListener(v -> {
            if (layoutRegistroMovimiento.getVisibility() == View.GONE) {
                layoutRegistroMovimiento.setVisibility(View.VISIBLE);
                layoutFiltros.setVisibility(View.GONE);
                recyclerViewMovimientos.setVisibility(View.GONE);

                btnMostrarFormulario.setText("Ocultar Formulario");
            } else {
                layoutRegistroMovimiento.setVisibility(View.GONE);
                btnMostrarFormulario.setText("Registrar Nuevo Movimiento Lima Pass");
                layoutFiltros.setVisibility(View.VISIBLE);
                recyclerViewMovimientos.setVisibility(View.VISIBLE);
            }
            clearFields();
        });

        btnAplicarFiltro.setOnClickListener(v -> aplicarFiltroFecha());
        btnLimpiarFiltro.setOnClickListener(v -> limpiarFiltroFecha());

        movimientosList = new ArrayList<>();
        movimientoAdapter = new MovimientoAdapter(movimientosList);
        recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMovimientos.setAdapter(movimientoAdapter);

        cargarMovimientosLimaPass();
    }

    private void showDatePicker(TextInputEditText targetEditText, Calendar targetCalendar) {
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            targetCalendar.set(year, month, day);
            updateDateField(targetEditText, targetCalendar.getTime());
        }, targetCalendar.get(Calendar.YEAR), targetCalendar.get(Calendar.MONTH), targetCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField(TextInputEditText targetEditText, Date date) {
        targetEditText.setText(dateFormat.format(date));
    }

    private void guardarMovimiento() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Debe iniciar sesión para guardar movimientos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String idTarjeta = etIdTarjeta.getText().toString().trim();
        Date fechaMovimiento = calendarRegistro.getTime();
        String paraderoEntrada = etParaderoEntrada.getText().toString().trim();
        String paraderoSalida = etParaderoSalida.getText().toString().trim();

        if (idTarjeta.isEmpty() || paraderoEntrada.isEmpty() || paraderoSalida.isEmpty()) {
            Toast.makeText(getContext(), "Complete todos los campos para Lima Pass.", Toast.LENGTH_SHORT).show();
            return;
        }

        Movimiento nuevoMovimiento = new Movimiento(userId, idTarjeta, fechaMovimiento, paraderoEntrada, paraderoSalida);

        db.collection("usuarios")
                .document(userId)
                .collection("movimientos")
                .add(nuevoMovimiento)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Movimiento Lima Pass guardado exitosamente!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    layoutRegistroMovimiento.setVisibility(View.GONE);
                    layoutFiltros.setVisibility(View.VISIBLE);
                    recyclerViewMovimientos.setVisibility(View.VISIBLE);
                    btnMostrarFormulario.setText("Registrar Nuevo Movimiento Lima Pass");
                    cargarMovimientosLimaPass();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearFields() {
        etIdTarjeta.setText("");
        etParaderoEntrada.setText("");
        etParaderoSalida.setText("");
        calendarRegistro = Calendar.getInstance();
        updateDateField(etFechaMovimiento, calendarRegistro.getTime());
    }

    private void aplicarFiltroFecha() {
        try {
            fechaFiltroInicioSeleccionada = dateFormat.parse(etFechaInicioFiltro.getText().toString().trim());
            fechaFiltroFinSeleccionada = dateFormat.parse(etFechaFinFiltro.getText().toString().trim());

            Calendar calFin = Calendar.getInstance();
            calFin.setTime(fechaFiltroFinSeleccionada);
            calFin.set(Calendar.HOUR_OF_DAY, 23);
            calFin.set(Calendar.MINUTE, 59);
            calFin.set(Calendar.SECOND, 59);
            calFin.set(Calendar.MILLISECOND, 999);
            fechaFiltroFinSeleccionada = calFin.getTime();
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Formato de fecha inválido.", Toast.LENGTH_SHORT).show();
        }
        cargarMovimientosLimaPass();
    }

    private void limpiarFiltroFecha() {
        etFechaInicioFiltro.setText("");
        etFechaFinFiltro.setText("");
        fechaFiltroInicioSeleccionada = null;
        fechaFiltroFinSeleccionada = null;
        cargarMovimientosLimaPass();
    }

    private void cargarMovimientosLimaPass() {
        if (currentUser == null) return;

        Query query = db.collection("usuarios")
                .document(currentUser.getUid())
                .collection("movimientos")
                .whereEqualTo("tipoTarjeta", "LimaPass");

        if (fechaFiltroInicioSeleccionada != null && fechaFiltroFinSeleccionada != null) {
            query = query.whereGreaterThanOrEqualTo("fechaMovimiento", fechaFiltroInicioSeleccionada)
                    .whereLessThanOrEqualTo("fechaMovimiento", fechaFiltroFinSeleccionada);
        }

        query = query.orderBy("fechaMovimiento", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
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
                Toast.makeText(getContext(), "Error al cargar movimientos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
