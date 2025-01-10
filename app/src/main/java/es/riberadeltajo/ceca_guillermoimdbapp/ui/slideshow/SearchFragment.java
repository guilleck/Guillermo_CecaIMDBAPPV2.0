package es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.R;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private FavoritesManager favoritesManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout
        View root = inflater.inflate(R.layout.fragment_search, container, false);

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

}
