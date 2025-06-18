package com.example.lab6_20212591;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister, btnFacebookLogin, btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Configuración de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configuración de Facebook
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult.getAccessToken().getToken());
                handleFacebookAccessToken(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(LoginActivity.this, "Inicio de sesión con Facebook cancelado.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
                Toast.makeText(LoginActivity.this, "Error con inicio de sesión de Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebookLogin = findViewById(R.id.btn_facebook_login);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> navigateToRegisterActivity());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebookLogin.setOnClickListener(v -> signInWithFacebook());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("El correo electrónico no puede estar vacío.");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("La contraseña no puede estar vacía.");
            etPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        String errorMessage = "Fallo de autenticación: " + task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
        startActivity(intent);

    }


    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Error de Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential Google:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();
                            String email = user.getEmail();
                            String displayName = user.getDisplayName();

                            String nombre = "";
                            String apellidos = "";

                            if (displayName != null) {
                                String[] partes = displayName.split(" ", 2);
                                nombre = partes[0];
                                apellidos = partes.length > 1 ? partes[1] : "";
                            }

                            Usuario nuevoUsuario = new Usuario(uid, email, nombre, apellidos, "");

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("usuarios").document(uid).set(nuevoUsuario)
                                    .addOnSuccessListener(unused -> Log.d(TAG, "Usuario registrado en Firestore"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error al guardar usuario", e));
                        }

                        Toast.makeText(LoginActivity.this, "Inicio de sesión con Google exitoso.", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential Google:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Fallo de autenticación con Google: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookAccessToken(String token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential Facebook:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();
                            String email = user.getEmail();
                            String displayName = user.getDisplayName();

                            String nombre = "";
                            String apellidos = "";

                            if (displayName != null) {
                                String[] partes = displayName.split(" ", 2);
                                nombre = partes[0];
                                apellidos = partes.length > 1 ? partes[1] : "";
                            }

                            Usuario nuevoUsuario = new Usuario(uid, email, nombre, apellidos, "");

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("usuarios").document(uid).set(nuevoUsuario)
                                    .addOnSuccessListener(unused -> Log.d(TAG, "Usuario registrado en Firestore"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error al guardar usuario", e));
                        }

                        Toast.makeText(LoginActivity.this, "Inicio de sesión con Facebook exitoso.", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential Facebook:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Fallo de autenticación con Facebook: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}