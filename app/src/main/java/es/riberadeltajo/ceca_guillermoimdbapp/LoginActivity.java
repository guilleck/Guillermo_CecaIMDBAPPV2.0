

package es.riberadeltajo.ceca_guillermoimdbapp;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private SignInButton signInButton;

    // Activity result para manejar la respuesta de Google Sign-In
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Si el inicio de sesión fue exitoso
                                        Toast.makeText(LoginActivity.this, "Sesión iniciada con éxito", Toast.LENGTH_SHORT).show();
                                        navigateToMainActivity(); // Redirigir a MainActivity
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario ya está autenticado
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // Si ya hay una sesión, redirigir a MainActivity
            navigateToMainActivity();
            return; // Evitar cargar la vista de inicio de sesión
        }

        // Configurar la vista de inicio de sesión
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        // Configuración de Google Sign-In
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options);

        // Configurar el botón de inicio de sesión
        signInButton = findViewById(R.id.sign_in_button);
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setText("Sign in with Google");
                break;
            }
        }

        // Listener del botón de inicio de sesión
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(signInIntent);
            }
        });
    }

    /**
     * Método para redirigir a MainActivity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cierra LoginActivity para evitar regresar con el botón de atrás
    }
}
