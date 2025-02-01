
package es.riberadeltajo.ceca_guillermoimdbapp;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.riberadeltajo.ceca_guillermoimdbapp.api.FacebookApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.models.FacebookTokenManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.UserProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private SignInButton signInButton;
    private LoginButton buttonFacebook;
    private CallbackManager callbackManager;
    private FacebookTokenManager tokenManager;
    private EditText editTextEmail, editTextContraseña;
    private Button buttonRegister, buttonLogin;

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
                                        Toast.makeText(LoginActivity.this, "Sesión iniciada con éxito", Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            String userID = user.getUid();
                                            String nombre = user.getDisplayName();
                                            String email = user.getEmail();


                                            registrarLastLogin(userID,nombre,email,(user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "");

                                            navegarMainActivity();
                                        }
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

        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            navegarMainActivity();
            return;
        }
        tokenManager = FacebookTokenManager.getInstance(this);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options);

        signInButton = findViewById(R.id.sign_in_button);
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setText("Sign in with Google");
                break;
            }
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activityResultLauncher.launch(signInIntent);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        buttonFacebook = findViewById(R.id.buttonFacebook);
        buttonFacebook.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        buttonFacebook.setPermissions(Arrays.asList("email", "public_profile"));

        buttonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error al iniciar sesión con Facebook", Toast.LENGTH_SHORT).show();
            }
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextContraseña = findViewById(R.id.editTextPassWord);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonRegister.setOnClickListener(v -> {
            registrarUsuario();
        });

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextContraseña.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "No se puede dejar ningún campo vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!correoCorrecto(email)){
                Toast.makeText(LoginActivity.this,"El formato del correo no es el correcto", Toast.LENGTH_SHORT).show();
            }

            iniciarSesion(email, password);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null || token.isExpired()) {
            Toast.makeText(this, "Token de Facebook no válido o expirado", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Sesión iniciada con Facebook", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userID = user.getUid();
                            String nombre = user.getDisplayName();
                            String email = user.getEmail();

                            registrarLastLogin(userID,nombre,email,(user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "");

                            navegarMainActivity();
                        }

                    } else {
                        Log.e("FacebookAuthError", "Error al autenticar con Facebook", task.getException());
                        Toast.makeText(LoginActivity.this, "Error al autenticar con Facebook: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        navegarMainActivity();
                    }
                });
    }


    private void fetchFacebookUserProfile() {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null) {
            return;
        }

        // Configuración de Retrofit para la API de Facebook
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request modifiedRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://graph.facebook.com/v12.0/") // Asegúrate de usar la versión correcta de la API
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FacebookApiService apiService = retrofit.create(FacebookApiService.class);
        Call<UserProfile> call = apiService.getUserProfile("id,name,email,picture");
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {

            }
        });
    }
    private void navegarMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void registrarLastLogin(String userId, String name, String email, String photoUrl) {
        String fechaLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(this);

        // Verificar si el usuario ya existe en la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + FavoritesDatabaseHelper.COLUMN_PHONE + ", " +
                FavoritesDatabaseHelper.COLUMN_ADDRESS + ", " + FavoritesDatabaseHelper.COLUMN_IMAGE +
                " FROM " + FavoritesDatabaseHelper.TABLE_USERS +
                " WHERE " + FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId});

        String phone = null;
        String address = null;
        String existingPhotoUrl = null;

        if (cursor != null && cursor.moveToFirst()) {
            phone = cursor.getString(0);
            address = cursor.getString(1);
            existingPhotoUrl = cursor.getString(2);
            cursor.close();
        }

        db.close();

        if (phone == null && address == null && existingPhotoUrl == null) {
            // No existe, insertar con campos proporcionados y los nuevos campos
            dbHelper.insertOrUpdateUser(
                    userId,
                    name,
                    email,
                    fechaLogin,
                    null,
                    "", // phone
                    "", // address
                    (photoUrl != null) ? photoUrl : ""
            );
        } else {
            dbHelper.updateLastLogin(userId, fechaLogin);

            if (photoUrl != null && !photoUrl.isEmpty() && existingPhotoUrl == null) {
                dbHelper.updatePhotoUrl(userId, photoUrl);
            }
        }

        // Actualizar las preferencias
        SharedPreferences preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

    }

    private void registrarUsuario() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextContraseña.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this,"No puede haber ningun campo vacio",Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(LoginActivity.this,"El formato de correo es invalido",Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this,"No puede haber ningun campo vacio",Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(LoginActivity.this,"La constraseña debe tener mas de 6 digitos",Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser usuario = auth.getCurrentUser();
                        if (usuario != null) {
                            String uid = usuario.getUid();
                            String nombre = usuario.getDisplayName();
                            if (nombre == null || nombre.isEmpty()) {
                                nombre = email.split("@")[0];
                            }
                            String emailUser = usuario.getEmail();

                            registrarUsuarioEnBaseDatos(uid, nombre, emailUser);

                           navegarMainActivity();
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // El correo ya está registrado
                            FirebaseAuthUserCollisionException exception = (FirebaseAuthUserCollisionException) task.getException();
                            String existingEmail = email;

                            if (existingEmail != null && !existingEmail.isEmpty()) {
                                auth.fetchSignInMethodsForEmail(existingEmail)
                                        .addOnCompleteListener(fetchTask -> {
                                            if (fetchTask.isSuccessful()) {
                                                List<String> signInMethods = fetchTask.getResult().getSignInMethods();
                                                boolean isGoogle = signInMethods != null && signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD);
                                                boolean isFacebook = signInMethods != null && signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD);

                                                if (isGoogle || isFacebook) {
                                                    Toast.makeText(LoginActivity.this, "Ese correo ya está registrado con otro tipo de inicio de sesión. Por favor, intenta iniciar sesión.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Ese correo ya está registrado. Intenta iniciar sesión.", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                // Error al obtener los métodos de inicio de sesión
                                                Toast.makeText(LoginActivity.this, "Error al verificar el correo.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // existingEmail es null o vacío, no se puede verificar los métodos de inicio de sesión
                                Toast.makeText(LoginActivity.this, "Ese correo ya está registrado. Intenta iniciar sesión.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Otros errores
                            Toast.makeText(LoginActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void registrarUsuarioEnBaseDatos(String userId, String name, String email) {
        String fechaLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(this);

        dbHelper.insertOrUpdateUser(
                userId,
                name,
                email,
                fechaLogin,
                null,
                "",
                "",
                ""
        );

    }

    private void iniciarSesion(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            String fechaLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                            FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(this);
                            dbHelper.updateLastLogin(userId, fechaLogin);

                            navegarMainActivity();
                        }
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean correoCorrecto(String correo){
        String correoRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,6}$";
        return correo.matches(correoRegex);
    }



}
