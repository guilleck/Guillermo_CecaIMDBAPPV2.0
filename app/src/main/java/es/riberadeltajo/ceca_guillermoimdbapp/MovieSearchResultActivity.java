package es.riberadeltajo.ceca_guillermoimdbapp;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.api.TMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.models.SearchResponse;
import es.riberadeltajo.ceca_guillermoimdbapp.models.TMDBMovie;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieSearchResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private TextView yearTextView, genreTextView;
    private FavoritesManager favoritesManager;
    private static final String TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5NmI0NWY5MmNkYWNhZjY3NDFlNWVmMTA1MzY1MDkwNyIsIm5iZiI6MTczNjUzOTExMS4zNDUsInN1YiI6IjY3ODE3YmU3Mzg4MWM3OTQxOWJiNzcxNyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.yNCqvMpnCqFwGPCHxfoSA1sO_8boWww7SRYrpeEsWJ0"; // Asegúrate de usar tu clave de API.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);

        recyclerView = findViewById(R.id.recyclerViewSearchResults);


        Intent intent = getIntent();
        String year = intent.getStringExtra("year");
        int genreId = intent.getIntExtra("genreId", -1);

        favoritesManager = new FavoritesManager(this);
        movieAdapter = new MovieAdapter(this, movieList, favoritesManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(movieAdapter);

        fetchMoviesByYearAndGenre(year, genreId);
    }

    private void fetchMoviesByYearAndGenre(String year, int genreId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + TMDB_API_KEY)
                            .addHeader("accept", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDBApiService tmdbApiService = retrofit.create(TMDBApiService.class);

        Call<SearchResponse> call = tmdbApiService.searchMovies(Integer.parseInt(year), genreId, "es-ES", "popularity.desc", 1);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TMDBMovie> results = response.body().getResults();
                    if (results != null && !results.isEmpty()) {
                        movieList.clear();
                        for (TMDBMovie tmdbMovie : results) {
                            Movie movie = new Movie();
                            movie.setId(String.valueOf(tmdbMovie.getId()));
                            movie.setTitle(tmdbMovie.getTitle());
                            movie.setReleaseDate(tmdbMovie.getRelease_date());
                            movie.setRating(String.valueOf(tmdbMovie.getVote_average()));
                            movie.setPosterPath("https://image.tmdb.org/t/p/w500" + tmdbMovie.getPoster_path());
                            movieList.add(movie);
                        }
                        movieAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MovieSearchResultActivity.this, "No se encontraron películas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MovieSearchResultActivity.this, "Error en la solicitud de API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Toast.makeText(MovieSearchResultActivity.this, "Error al obtener las películas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
