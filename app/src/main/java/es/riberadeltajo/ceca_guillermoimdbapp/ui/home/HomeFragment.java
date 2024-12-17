package es.riberadeltajo.ceca_guillermoimdbapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.R;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(getContext(), movieList);
        recyclerView.setAdapter(movieAdapter);

        // Cargar datos desde la API
        loadTopMovies();

        return view;
    }

    private void loadTopMovies() {
        IMDBApiService apiService = new IMDBApiService();
        apiService.getTopMeterTitles("ALL", new IMDBApiService.IMDBApiCallback() {
            @Override
            public void onSuccess(String response) {
                // Aquí parsearías la respuesta y actualizarías movieList
                // Esto requiere un parser JSON adecuado
                // Placeholder:
                movieList.add(new Movie("The Example Movie", "https://example.com/poster.jpg"));
                getActivity().runOnUiThread(() -> movieAdapter.notifyDataSetChanged());
            }

            @Override
            public void onError(String error) {
                // Manejar errores
            }
        });
    }
}
