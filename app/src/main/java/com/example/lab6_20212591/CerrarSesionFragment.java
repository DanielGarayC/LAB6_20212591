package com.example.lab6_20212591;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.example.lab6_20212591.R;
import com.example.lab6_20212591.MainActivity;

public class CerrarSesionFragment extends Fragment {

    public CerrarSesionFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Puedes inflar un layout muy simple si quieres que haya algo visual por un instante,
        // o null si no quieres mostrar nada, aunque es mejor tener un layout simple.
        // Por ejemplo, un TextView que diga "Cerrando sesión..."
        return inflater.inflate(R.layout.fragment_cerrar_sesion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Llama a signOut inmediatamente después de que el fragmento se ha creado.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).signOut();
        }
    }
}