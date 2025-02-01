package es.riberadeltajo.ceca_guillermoimdbapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.share.Share;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.databinding.ActivityMainBinding;
import es.riberadeltajo.ceca_guillermoimdbapp.models.KeyStoreManager;
import es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow.EditUserFragment;
import es.riberadeltajo.ceca_guillermoimdbapp.utils.AppLifecycleManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private com.google.android.material.imageview.ShapeableImageView imageViewPhoto;
    private TextView textViewNombre, textViewEmail;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        setSupportActionBar(binding.appBarMain.toolbar);

        auth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            registrarLastLogin(user);
        }else{
           redirectToLogin();
        }

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        textViewNombre = headerView.findViewById(R.id.textViewNombre);
        textViewEmail = headerView.findViewById(R.id.textViewEmail);
        imageViewPhoto = headerView.findViewById(R.id.imageViewPhoto);
        logoutButton = headerView.findViewById(R.id.buttonLogout);


        String providerId = getProviderId(user);
        if ("google.com".equals(providerId)) {
            textViewNombre.setText(user.getDisplayName());
            textViewEmail.setText(user.getEmail());
        } else if ("facebook.com".equals(providerId)) {
            textViewNombre.setText(user.getDisplayName());
            textViewEmail.setText("Conectado con Facebook");
        } else {
            textViewNombre.setText(user.getDisplayName());
            textViewEmail.setText(user.getEmail());
        }
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(imageViewPhoto);
        }else{
                imageViewPhoto.setImageResource(R.drawable.usuario);
        }

        loadProfileImage();

        logoutButton.setOnClickListener(v -> {
            signOut();
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_buscar)
                .setOpenableLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_top_10) {
                navController.navigate(R.id.nav_home);
            } else if (id == R.id.nav_favoritas) {
                navController.navigate(R.id.nav_search);
            } else if (id == R.id.nav_buscar) {
                navController.navigate(R.id.nav_buscarPeli);
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });


        AppLifecycleManager appLifecycleManager = new AppLifecycleManager(MainActivity.this);

        // Registra los métodos del ciclo de vida
        getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                appLifecycleManager.onActivityStarted(MainActivity.this);
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                appLifecycleManager.onActivityStopped(MainActivity.this);
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                appLifecycleManager.onActivityResumed(MainActivity.this);
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                appLifecycleManager.onActivityPaused(MainActivity.this);
            }
        });

    }

    private void loadUserProfile() {
        FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT name, address, phone, image FROM " + FavoritesDatabaseHelper.TABLE_USERS +
                            " WHERE " + FavoritesDatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{user.getUid()}
            );

            SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
            String savedName = sharedPreferences.getString("name", null);
            String savedAddress = sharedPreferences.getString("address", null);
            String savedPhone = sharedPreferences.getString("phone", null);
            String savedImageUri = sharedPreferences.getString("profile_image_uri", null);

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String phone = cursor.getString(cursor.getColumnIndex("phone"));
                String imageUri = cursor.getString(cursor.getColumnIndex("image"));

                cursor.close();
                db.close();

                // Priorizar los datos de SharedPreferences si existen
                textViewNombre.setText(savedName != null ? savedName : (name != null ? name : "Usuario"));
                textViewEmail.setText(user.getEmail());

                String finalImageUri = savedImageUri != null ? savedImageUri : imageUri;

                if (finalImageUri != null && !finalImageUri.isEmpty()) {
                    Glide.with(this).load(Uri.parse(finalImageUri)).into(imageViewPhoto);
                } else {
                    imageViewPhoto.setImageResource(R.drawable.usuario);
                }
            } else {
                db.close();
                Toast.makeText(this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
            }
        } else {
            redirectToLogin();
        }
    }


    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("Imagen", Context.MODE_PRIVATE);
        String imageUriString = prefs.getString("profile_image_uri", null);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this).load(imageUri).into(imageViewPhoto);
        } else {
            imageViewPhoto.setImageResource(R.drawable.usuario);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        if (id == R.id.action_editUser) {
            navController.navigate(R.id.nav_editUser);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private String getProviderId(FirebaseUser user) {
        if (user == null) {
            return "unknown";
        }

        for (UserInfo userInfo : user.getProviderData()) {
            String provider = userInfo.getProviderId();
            if ("google.com".equals(provider)) {
                return "google.com";
            } else if ("facebook.com".equals(provider)) {
                return "facebook.com";
            }
        }
        return "unknown";
    }


    private void signOut() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String providerId = getProviderId(user);
            String userId = user.getUid();
            String fechaLogout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(MainActivity.this);
            dbHelper.updateLastLogout(userId, fechaLogout);

            // Cerrar el cajón si está abierto
            DrawerLayout drawer = binding.drawerLayout;
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }

            // Manejo del cierre de sesión según el proveedor
            if ("google.com".equals(providerId)) {
                googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    Toast.makeText(MainActivity.this, "Sesión cerrada en Google", Toast.LENGTH_SHORT).show();
                    auth.signOut(); // Cerrar sesión en Firebase
                    redirectToLogin();
                });
            } else if ("facebook.com".equals(providerId)) {
                LoginManager.getInstance().logOut();
                auth.signOut(); // Cerrar sesión en Firebase
                Toast.makeText(MainActivity.this, "Sesión cerrada en Facebook", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            } else {
                // Cerrar sesión para otros proveedores o anónimos
                auth.signOut();
                Toast.makeText(MainActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        } else {
            redirectToLogin();
        }
    }


    private void registrarLastLogin(FirebaseUser user) {
        if (user == null) return;

        String userId = user.getUid();
        String email = user.getEmail() != null ? user.getEmail() : "Sin email";
        String fechaLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());



        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String savedName = sharedPreferences.getString("name", null);
        String savedAddress = sharedPreferences.getString("address", null);
        String savedPhone = sharedPreferences.getString("phone", null);

        SharedPreferences prefs = getSharedPreferences("Imagen", Context.MODE_PRIVATE);
        String imageUriString = prefs.getString("profile_image_uri", null);

        KeyStoreManager keyStoreManager = new KeyStoreManager(this);
        String addressEncriptado = savedAddress != null ? keyStoreManager.encrypt(savedAddress) : null;
        String phoneEncriptado = savedPhone != null ? keyStoreManager.encrypt(savedPhone) : null;

        FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(this);

        // Insertar o actualizar el usuario en la base de datos
        dbHelper.insertOrUpdateUser(userId, savedName, email, fechaLogin, null, phoneEncriptado, addressEncriptado, imageUriString);
    }


    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadProfileImage();
    }


}