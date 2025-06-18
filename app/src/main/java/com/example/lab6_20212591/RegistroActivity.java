package com.example.lab6_20212591;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {

    // Cambiado de EditText a TextInputEditText para que coincida con el XML y sea más específico
    private TextInputEditText etRegEmail, etRegPassword, etRegConfirmPassword;
    private TextInputEditText etRegNombre, etRegApellidos, etRegDNI; // Nuevos TextInputEditTexts
    private MaterialButton btnRegister;
    private TextView tvLoginRedirect;
    private ProgressBar progressBarRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Instancia de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializar Firestore

        etRegEmail = findViewById(R.id.et_reg_email);
        etRegNombre = findViewById(R.id.et_reg_nombre);
        etRegApellidos = findViewById(R.id.et_reg_apellidos);
        etRegDNI = findViewById(R.id.et_reg_dni);
        etRegPassword = findViewById(R.id.et_reg_password);
        etRegConfirmPassword = findViewById(R.id.et_reg_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginRedirect = findViewById(R.id.tv_login_redirect);
        progressBarRegister = findViewById(R.id.progress_bar_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLoginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String email = etRegEmail.getText().toString().trim();
        String nombre = etRegNombre.getText().toString().trim();
        String apellidos = etRegApellidos.getText().toString().trim();
        String dni = etRegDNI.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("El correo electrónico es obligatorio.");
            etRegEmail.requestFocus();
            return;
        }
        // Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.setError("Ingrese un correo electrónico válido.");
            etRegEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nombre)) {
            etRegNombre.setError("El nombre es obligatorio.");
            etRegNombre.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(apellidos)) {
            etRegApellidos.setError("Los apellidos son obligatorios.");
            etRegApellidos.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(dni)) {
            etRegDNI.setError("El DNI es obligatorio.");
            etRegDNI.requestFocus();
            return;
        }
        if (dni.length() != 8) { // Validación de 8 dígitos para DNI
            etRegDNI.setError("El DNI debe tener 8 dígitos.");
            etRegDNI.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etRegPassword.setError("La contraseña es obligatoria.");
            etRegPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etRegPassword.setError("La contraseña debe tener al menos 6 caracteres.");
            etRegPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etRegConfirmPassword.setError("Las contraseñas no coinciden.");
            etRegConfirmPassword.requestFocus();
            return;
        }

        progressBarRegister.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarRegister.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Guardar información adicional del usuario en Firestore usando la clase Usuario
                                saveUserDataToFirestore(user.getUid(), email, nombre, apellidos, dni);
                            }
                        } else {
                            // Manejo de errores específicos de Firebase Authentication
                            String errorMessage = "Error de registro.";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                errorMessage = "La contraseña es muy débil.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "Ya existe una cuenta con este correo electrónico.";
                            } catch (Exception e) {
                                errorMessage = "Error: " + e.getMessage();
                            }
                            Toast.makeText(RegistroActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String uid, String email, String nombre, String apellidos, String dni) {
        // Crear una instancia de la clase Usuario
        Usuario nuevoUsuario = new Usuario(uid, email, nombre, apellidos, dni);

        db.collection("usuarios") // Colección donde se guardarán los datos del usuario (Cambiado a "usuarios")
                .document(uid) // Usar el UID como ID del documento para el usuario
                .set(nuevoUsuario) // Guardar el objeto Usuario directamente
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistroActivity.this, "Registro exitoso. ¡Inicia sesión!", Toast.LENGTH_SHORT).show();
                            // Redirigir al usuario a la actividad de Login
                            Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                            // Limpiar la pila de actividades para que el usuario no pueda volver al registro con el botón "atrás"
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegistroActivity.this, "Error al guardar datos de usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            // Es buena práctica eliminar el usuario de Authentication si falla el guardado en Firestore
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.delete().addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        Toast.makeText(RegistroActivity.this, "Cuenta de autenticación eliminada debido a error de datos.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RegistroActivity.this, "Error al eliminar cuenta de autenticación.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }
                });
    }
}