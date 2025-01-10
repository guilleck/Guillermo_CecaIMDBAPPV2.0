package es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.R;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class FavoriteFragment extends Fragment {
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private FavoritesManager favoritesManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Inicializamos el RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));  // Cambié el LinearLayoutManager a GridLayoutManager

        // Inicializamos la lista de películas y el adaptador
        favoritesManager = new FavoritesManager(getContext());
        adapter = new MovieAdapter(getContext(), movieList,favoritesManager);
        recyclerView.setAdapter(adapter);



        adapter.setOnItemLongClickListener(movie -> {
            // Eliminar la película de favoritos
            favoritesManager.removeFavorite(movie); // Eliminar de favoritos
            movieList.remove(movie); // Remover de la lista local
            adapter.notifyDataSetChanged(); // Notificar al adaptador que se ha eliminado la película
            Toast.makeText(getContext(), "Eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        Button shareButton = root.findViewById(R.id.btnShareFavorites);
        shareButton.setOnClickListener(v -> requestBluetoothPermissionAndShare());

        // Aquí puedes cargar los favoritos desde la base de datos si es necesario
        loadFavorites();

        return root;
    }

    private void loadFavorites() {
        // Aquí puedes cargar las películas desde la base de datos a la lista 'movieList'
        List<Movie> favorites = favoritesManager.getFavorites();
        movieList.clear();
        movieList.addAll(favorites);
        adapter.notifyDataSetChanged();
    }
    private void shareFavorites() {
        // Usamos Gson para convertir las películas a formato JSON
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(movieList);

        // Crear el Intent para compartir via Bluetooth u otras aplicaciones
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, jsonFavorites);

        // Verificar si hay una aplicación para compartir
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Compartir lista de favoritos"));
        } else {
            Toast.makeText(getContext(), "No hay aplicaciones disponibles para compartir.", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestBluetoothPermissionAndShare() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Si no se han concedido los permisos, solicitarlos
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
            }, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            // Si ya se tienen los permisos, compartir las películas
            shareFavorites();
        }
    }


    // Manejo de los resultados de la solicitud de permisos de Bluetooth
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, compartir las películas
                shareFavorites();
            } else {
                // Permiso denegado, mostrar un mensaje
                Toast.makeText(getContext(), "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
