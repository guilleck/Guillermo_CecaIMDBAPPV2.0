
package es.riberadeltajo.ceca_guillermoimdbapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


import es.riberadeltajo.ceca_guillermoimdbapp.R;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.MovieDetailsActivity;
import es.riberadeltajo.ceca_guillermoimdbapp.models.TMDBMovie;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private final Context context;
    private List<Movie> movieList = new ArrayList<>();
    private OnItemLongClickListener onItemLongClickListener;
    private FavoritesManager favoritesManager;


    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    public MovieAdapter(Context context, List<Movie> movieList, FavoritesManager favoritesManager) {
        this.context = context;
        this.movieList = movieList;
        this.favoritesManager = favoritesManager;
    }

    public MovieAdapter(Context context,List<Movie> movieList, String idUsuario, FavoritesDatabaseHelper databaseHelper, boolean b ) {
        this.context = context;
    }



    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Cargar la imagen del poster usando Glide
        Glide.with(context)
                .load(movie.getPosterPath())
                .into(holder.posterImageView);

        // Listener para cuando se pulsa sobre la película
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, MovieDetailsActivity.class);
            i.putExtra("pelicula", movie);
            context.startActivity(i);
        });
        // Cargar la imagen del poster usando Glide
        Glide.with(context)
                .load(movie.getPosterPath())
                .into(holder.posterImageView);

        holder.itemView.setOnLongClickListener(v -> {
            if (isMovieInFavorites(movie)) {
                // Si la película está en favoritos, la eliminamos
                favoritesManager.removeFavorite(movie);
                movieList.remove(position);  // Eliminar de la lista
                notifyItemRemoved(position);  // Notificar al adaptador
                Toast.makeText(context, "Eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                // Si la película no está en favoritos, la agregamos
                favoritesManager.addFavorite(movie);
                Toast.makeText(context, "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true;  // El evento fue manejado
        });


    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
    private boolean isMovieInFavorites(Movie movie) {
        List<Movie> favorites = favoritesManager.getFavorites();
        for (Movie m : favorites) {
            if (m.getId().equals(movie.getId())) {
                return true;
            }
        }
        return false;
    }

    public interface OnItemLongClickListener {
        void onLongClick(Movie movie);
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.ImageViewPelicula);
        }
    }
    public void updateMovies(List<Movie> newMovieList) {
        movieList.clear();
        movieList.addAll(newMovieList);
        notifyDataSetChanged();
    }


}
