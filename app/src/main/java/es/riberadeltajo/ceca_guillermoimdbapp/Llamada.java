package es.riberadeltajo.ceca_guillermoimdbapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import es.riberadeltajo.ceca_guillermoimdbapp.utils.AppLifecycleManager;

public class Llamada extends AppCompatActivity {

    private AppLifecycleManager appLifecycleManager;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_llamada);
        // Inicializar y registrar el AppLifeCycleManager
        appLifecycleManager = new AppLifecycleManager(this);
        registerActivityLifecycleCallbacks(appLifecycleManager);
        registerComponentCallbacks(appLifecycleManager);

        // Verificar si hay un logout pendiente
        appLifecycleManager.checkForPendingLogout();

    }


}