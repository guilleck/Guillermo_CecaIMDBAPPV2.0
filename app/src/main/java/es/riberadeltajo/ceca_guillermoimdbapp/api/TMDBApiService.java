package es.riberadeltajo.ceca_guillermoimdbapp.api;

import es.riberadeltajo.ceca_guillermoimdbapp.models.GeneroResponse;
import es.riberadeltajo.ceca_guillermoimdbapp.models.SearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDBApiService {

    @GET("movie/changes")
    Call<SearchResponse> getMovieChanges(@Query("page") int page);

    @GET("genre/movie/list")
    Call<GeneroResponse> getGenres(@Query("language") String language);

    @GET("discover/movie")
    Call<SearchResponse> searchMovies(
            @Query("primary_release_year") int primary_release_year,
            @Query("with_genres") int with_genres,
            @Query("language") String language,
            @Query("sort_by") String sort_by,
            @Query("page") int page
    );
}
