package es.riberadeltajo.ceca_guillermoimdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import es.riberadeltajo.ceca_guillermoimdbapp.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        auth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        TextView textViewNombre = headerView.findViewById(R.id.textViewNombre);
        TextView textViewEmail = headerView.findViewById(R.id.textViewEmail);
        com.google.android.material.imageview.ShapeableImageView imageViewPhoto = headerView.findViewById(R.id.imageViewPhoto);
        Button logoutButton = headerView.findViewById(R.id.buttonLogout);


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
        }

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
            if(id == R.id.nav_top_10){
                navController.navigate(R.id.nav_home);
            }else if(id == R.id.nav_favoritas){
                navController.navigate(R.id.nav_search);
            }else if(id == R.id.nav_buscar){
                navController.navigate(R.id.nav_buscarPeli);
            }
            return true;
        });
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

            if ("google.com".equals(providerId)) {
                // Cerrar sesión de Google
                googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    Toast.makeText(MainActivity.this, "Sesión cerrada en Google", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                });
            } else if ("facebook.com".equals(providerId)) {
                // Cerrar sesión de Facebook
                LoginManager.getInstance().logOut();
                auth.signOut(); // Asegurarse de cerrar sesión en Firebase también
                Toast.makeText(MainActivity.this, "Sesión cerrada en Facebook", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            } else {
                // Cerrar sesión de otros proveedores o anónimos
                auth.signOut();
                redirectToLogin();
            }
        } else {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}