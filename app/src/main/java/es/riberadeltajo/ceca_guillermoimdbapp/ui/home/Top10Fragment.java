
package es.riberadeltajo.ceca_guillermoimdbapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiClient;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.databinding.FragmentHomeBinding;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.models.PopularMoviesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Top10Fragment extends Fragment {

    private FragmentHomeBinding binding;
    private IMDBApiService ApiService;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter adapter;
    private RecyclerView recyclerView;
    private FavoritesDatabaseHelper databaseHelper;
    private FavoritesManager favoritesManager;
    private IMDBApiClient imdbApiClient;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        favoritesManager = new FavoritesManager(getContext());
        adapter = new MovieAdapter(getContext(), movieList,favoritesManager);
        recyclerView.setAdapter(adapter);



        Call<PopularMoviesResponse> call = IMDBApiClient.getApiService().obtenerTop10("US");
        Log.d("HomeFragment", "Usando clave API: " + IMDBApiClient.getApiKey());
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PopularMoviesResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    if (edges != null && !edges.isEmpty()) {
                        movieList.clear();
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            PopularMoviesResponse.Edge edge = edges.get(i);
                            PopularMoviesResponse.Node node = edge.getNode();
                            Movie movie = new Movie();
                            movie.setId(node.getId());
                            movie.setTitle(node.getTitleText().getText());
                            movie.setReleaseDate(node.getPrimaryImage().getUrl());
                            movie.setPosterPath(node.getPrimaryImage().getUrl());
                            movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }else if(response.code() == 429){
                    Log.e("HomeFragment", "Límite de solicitudes alcanzado. Cambiando API Key.");
                    IMDBApiClient.switchApiKey(); // Cambiar a la siguiente clave API
                    fetchMovies();
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });

        adapter.setOnItemLongClickListener(movie -> {
            String userID = getGoogleUserId();
            favoritesManager.addFavorite(movie,userID);
            movieList.add(movie);
            adapter.notifyItemInserted(movieList.size() - 1);
            Toast.makeText(getContext(), "Película añadida a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void fetchMovies() {
        Call<PopularMoviesResponse> call = IMDBApiClient.getApiService().obtenerTop10("US");
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PopularMoviesResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    if (edges != null && !edges.isEmpty()) {
                        movieList.clear();
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            PopularMoviesResponse.Edge edge = edges.get(i);
                            PopularMoviesResponse.Node node = edge.getNode();
                            Movie movie = new Movie();
                            movie.setId(node.getId());
                            movie.setTitle(node.getTitleText().getText());
                            movie.setPosterPath(node.getPrimaryImage().getUrl());
                            movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });
    }

    public String getGoogleUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
        @Override
        public void onResume() {
            super.onResume();
            fetchMovies();
        }


}







