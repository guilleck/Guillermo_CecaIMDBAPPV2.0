package es.riberadeltajo.ceca_guillermoimdbapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        recyclerView = findViewById(R.id.recyclerViewMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Simulación de datos
        movieList = new ArrayList<>();
        movieList.add(new Movie("Movie 1", "https://imageurl.com/poster1.jpg"));
        movieList.add(new Movie("Movie 2", "https://imageurl.com/poster2.jpg"));
        movieList.add(new Movie("Movie 3", "https://imageurl.com/poster3.jpg"));

        // Configurar adaptador
        movieAdapter = new MovieAdapter(this, movieList, movie -> {
            // Evento al hacer clic en una película
            Intent intent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("imageUrl", movie.getImageUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(movieAdapter);
    }
}
