package com.enriquecastillo.adopta_mascotas.ui.login.loginemail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enriquecastillo.adopta_mascotas.MainActivity;
import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.ui.login.LoginActivity;
import com.enriquecastillo.adopta_mascotas.ui.profile.ProfileEditActivity;
import com.enriquecastillo.adopta_mascotas.ui.register.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmailLoginActivity extends AppCompatActivity  {

    public static final int MIN_LENGTH_PASSWORD = 6;
    @BindView(R.id.toolbar)
    Toolbar myToolbar;
    @BindView(R.id.et_login_email)
    TextInputLayout edtLoginEmail;
    @BindView(R.id.et_login_password)
    TextInputLayout edtLoginPassword;
    @BindView(R.id.progress_bar_signin)
    ProgressBar progressBarSignIn;

    private static final String TAG = "EmailLoginActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        ButterKnife.bind(this);
        setUpToolbar();
        initializeFirebase();

        progressBarSignIn.setVisibility(View.INVISIBLE);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Log.w(TAG, "onAuthStateChanged - signed_in" + firebaseUser.getEmail());
                } else {
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }
            }
        };
    }

    private void setUpToolbar() {
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Continuar con Correo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendAndSaveEmailPassword(String email, String password) {
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
        signIn(email, password);
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                        Toast.makeText(EmailLoginActivity.this, "Cuenta Facebook iniciada con exito!", Toast.LENGTH_SHORT).show();
                        goToEditProfileActivity();
                    } else {
                        goToMainActivity();
                    }
                } else {
                    String error = task.getException().getMessage();
                    if (error.equals(getString(R.string.Error_password_incorrect))) {
                        Toast.makeText(EmailLoginActivity.this, "La contraseña es inavalida o el usuario no coloco la contaseña.", Toast.LENGTH_LONG).show();
                        edtLoginPassword.setError("Contraseña Incorrecta");
                        progressBarSignIn.setVisibility(View.INVISIBLE);
                    } else if (error.equals(getString(R.string.Error_email_dont_exist))) {
                        Toast.makeText(EmailLoginActivity.this, "Por favor crea una cuenta!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EmailLoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }


                }
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToEditProfileActivity() {
        Intent intent = new Intent(EmailLoginActivity.this, ProfileEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }


    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @OnClick(R.id.btn_sign_in)
    void onSignInClick() {
        edtLoginEmail.setError(null);
        edtLoginPassword.setError(null);
        if (TextUtils.isEmpty(edtLoginEmail.getEditText().getText())) {
            edtLoginEmail.setError("Llena bien este campo");
            return;
        }
        if (!isValidEmail(edtLoginEmail.getEditText().getText().toString())) {
            edtLoginEmail.setError("Digita un correo valido");
            return;
        }

        if (TextUtils.isEmpty(edtLoginPassword.getEditText().getText())) {
            edtLoginPassword.setError("Llena bien este campo");
            return;
        }

        if (MIN_LENGTH_PASSWORD > edtLoginPassword.getEditText().getText().toString().length() ) {
            edtLoginPassword.setError("La contraseña debe tener mas de 6 caracteres");
            return;
        }

        String email = edtLoginEmail.getEditText().getText().toString().trim();
        String password = edtLoginPassword.getEditText().getText().toString().trim();
        sendAndSaveEmailPassword(email, password);
        progressBarSignIn.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
        progressBarSignIn.setVisibility(View.INVISIBLE);
    }
}
