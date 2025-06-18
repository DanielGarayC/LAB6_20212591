package com.example.lab6_20212591;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.lab6_20212591.R;
import com.example.lab6_20212591.Movimiento;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ResumenFragment extends Fragment {

    private static final String TAG = "ResumenFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private BarChart barChartMovimientos;
    private PieChart pieChartUsoTarjetas;
    private TextInputEditText etFiltroFechaInicio, etFiltroFechaFin;
    private MaterialButton btnAplicarFiltro;

    private Calendar calendarInicio = Calendar.getInstance();
    private Calendar calendarFin = Calendar.getInstance();

    public ResumenFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resumen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChartMovimientos = view.findViewById(R.id.barChartMovimientos);
        pieChartUsoTarjetas = view.findViewById(R.id.pieChartUsoTarjetas);
        etFiltroFechaInicio = view.findViewById(R.id.et_filtro_resumen_fecha_inicio);
        etFiltroFechaFin = view.findViewById(R.id.et_filtro_resumen_fecha_fin);
        btnAplicarFiltro = view.findViewById(R.id.btn_aplicar_filtro_resumen);

        etFiltroFechaInicio.setOnClickListener(v -> showDatePicker(etFiltroFechaInicio, calendarInicio));
        etFiltroFechaFin.setOnClickListener(v -> showDatePicker(etFiltroFechaFin, calendarFin));
        btnAplicarFiltro.setOnClickListener(v -> cargarDatosYActualizarGraficos());

        cargarDatosYActualizarGraficos();
    }

    private void showDatePicker(TextInputEditText editText, Calendar calendarInstance) {
        new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
            calendarInstance.set(Calendar.YEAR, year);
            calendarInstance.set(Calendar.MONTH, monthOfYear);
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField(editText, calendarInstance.getTime());
        }, calendarInstance.get(Calendar.YEAR), calendarInstance.get(Calendar.MONTH), calendarInstance.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField(TextInputEditText editText, Date date) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editText.setText(sdf.format(date));
    }

    private void cargarDatosYActualizarGraficos() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Sesión no iniciada para ver resumen.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date fechaInicio = null;
        Date fechaFin = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (!etFiltroFechaInicio.getText().toString().isEmpty()) {
                fechaInicio = sdf.parse(etFiltroFechaInicio.getText().toString());
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(fechaInicio);
                tempCal.set(Calendar.HOUR_OF_DAY, 0);
                tempCal.set(Calendar.MINUTE, 0);
                tempCal.set(Calendar.SECOND, 0);
                tempCal.set(Calendar.MILLISECOND, 0);
                fechaInicio = tempCal.getTime();
            }
            if (!etFiltroFechaFin.getText().toString().isEmpty()) {
                fechaFin = sdf.parse(etFiltroFechaFin.getText().toString());
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(fechaFin);
                tempCal.set(Calendar.HOUR_OF_DAY, 23);
                tempCal.set(Calendar.MINUTE, 59);
                tempCal.set(Calendar.SECOND, 59);
                tempCal.set(Calendar.MILLISECOND, 999);
                fechaFin = tempCal.getTime();
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Formato de fecha inválido.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parseando fecha", e);
            return;
        }

        com.google.firebase.firestore.Query query = db
                .collection("usuarios")
                .document(currentUser.getUid())
                .collection("movimientos");

        if (fechaInicio != null) {
            query = query.whereGreaterThanOrEqualTo("fechaMovimiento", fechaInicio);
        }
        if (fechaFin != null) {
            query = query.whereLessThanOrEqualTo("fechaMovimiento", fechaFin);
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Movimiento> movimientos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            movimientos.add(document.toObject(Movimiento.class));
                        }
                        procesarDatosParaGraficos(movimientos);
                    } else {
                        Log.e(TAG, "Error al cargar datos para gráficos: ", task.getException());
                        Toast.makeText(getContext(), "Error al cargar datos para gráficos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void procesarDatosParaGraficos(List<Movimiento> movimientos) {
        Map<String, Map<String, Integer>> movimientosPorTipoYMes = new HashMap<>();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat monthNameFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        for (Movimiento mov : movimientos) {
            String tipo = mov.getTipoTarjeta(); // "Linea1" o "LimaPass"
            String mesAnio = monthYearFormat.format(mov.getFechaMovimiento());
            movimientosPorTipoYMes.computeIfAbsent(tipo, k -> new TreeMap<>())
                    .merge(mesAnio, 1, Integer::sum);
        }

        List<String> mesesOrdenadosUnicos = new ArrayList<>();
        for (Map<String, Integer> map : movimientosPorTipoYMes.values()) {
            for (String mes : map.keySet()) {
                if (!mesesOrdenadosUnicos.contains(mes)) {
                    mesesOrdenadosUnicos.add(mes);
                }
            }
        }
        Collections.sort(mesesOrdenadosUnicos);

        ArrayList<BarEntry> entriesTren = new ArrayList<>();
        ArrayList<BarEntry> entriesBus = new ArrayList<>();
        ArrayList<String> xAxisLabels = new ArrayList<>();

        for (int i = 0; i < mesesOrdenadosUnicos.size(); i++) {
            String mesAnio = mesesOrdenadosUnicos.get(i);
            try {
                xAxisLabels.add(monthNameFormat.format(monthYearFormat.parse(mesAnio)));
            } catch (ParseException e) {
                xAxisLabels.add(mesAnio);
                Log.e(TAG, "Error al parsear mes para etiqueta de eje X", e);
            }

            entriesTren.add(new BarEntry(i, movimientosPorTipoYMes.getOrDefault("Linea1", new HashMap<>()).getOrDefault(mesAnio, 0)));
            entriesBus.add(new BarEntry(i, movimientosPorTipoYMes.getOrDefault("LimaPass", new HashMap<>()).getOrDefault(mesAnio, 0)));
        }
        setupBarChart(entriesTren, entriesBus, xAxisLabels);

        int conteoTren = 0;
        int conteoBus = 0;
        for (Movimiento mov : movimientos) {
            if (mov.getTipoTarjeta().equals("Linea1")) {
                conteoTren++;
            } else if (mov.getTipoTarjeta().equals("LimaPass")) {
                conteoBus++;
            }
        }
        setupPieChart(conteoTren, conteoBus);
    }

    private void setupBarChart(ArrayList<BarEntry> entriesTren, ArrayList<BarEntry> entriesBus, ArrayList<String> xAxisLabels) {
        BarDataSet dataSetTren = new BarDataSet(entriesTren, "Tren (Línea 1)");
        dataSetTren.setColor(Color.parseColor("#0E2194"));
        dataSetTren.setValueTextColor(Color.BLACK);
        dataSetTren.setValueTextSize(10f);

        BarDataSet dataSetBus = new BarDataSet(entriesBus, "Bus (Lima Pass)");
        dataSetBus.setColor(Color.parseColor("#1877F2"));
        dataSetBus.setValueTextColor(Color.BLACK);
        dataSetBus.setValueTextSize(10f);

        BarData barData = new BarData(dataSetTren, dataSetBus);
        barData.setBarWidth(0.35f);
        barData.groupBars(0f, 0.3f, 0.02f);

        barChartMovimientos.setData(barData);
        barChartMovimientos.getDescription().setEnabled(false);
        barChartMovimientos.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        barChartMovimientos.getXAxis().setGranularity(1f);
        barChartMovimientos.getXAxis().setCenterAxisLabels(true);
        barChartMovimientos.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartMovimientos.getAxisRight().setEnabled(false);
        barChartMovimientos.animateY(1000);
        barChartMovimientos.invalidate();

        Legend legend = barChartMovimientos.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYOffset(10f);
        legend.setXOffset(10f);
        legend.setYEntrySpace(5f);
    }

    private void setupPieChart(int conteoTren, int conteoBus) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (conteoTren > 0) entries.add(new PieEntry(conteoTren, "Tren"));
        if (conteoBus > 0) entries.add(new PieEntry(conteoBus, "Bus"));

        PieDataSet dataSet = new PieDataSet(entries, "Uso de Tarjetas");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#0E2194"));
        colors.add(Color.parseColor("#1877F2"));
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(dataSet);
        pieChartUsoTarjetas.setData(pieData);
        pieChartUsoTarjetas.getDescription().setEnabled(false);
        pieChartUsoTarjetas.setCenterText("Total Viajes\n" + (conteoTren + conteoBus));
        pieChartUsoTarjetas.setCenterTextSize(14f);
        pieChartUsoTarjetas.setCenterTextColor(Color.BLACK);
        pieChartUsoTarjetas.setEntryLabelColor(Color.BLACK);
        pieChartUsoTarjetas.setEntryLabelTextSize(10f);
        pieChartUsoTarjetas.animateY(1000);
        pieChartUsoTarjetas.invalidate();

        Legend legend = pieChartUsoTarjetas.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }
}
