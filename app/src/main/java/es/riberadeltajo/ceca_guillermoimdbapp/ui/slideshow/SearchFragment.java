package es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import es.riberadeltajo.ceca_guillermoimdbapp.MovieSearchResultActivity;
import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.api.TMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.databinding.FragmentSearchBinding;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Genero;
import es.riberadeltajo.ceca_guillermoimdbapp.models.GeneroResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private TMDBApiService ApiService;
    private Spinner spinnerGeneros;
    private List<Genero> generosList = new ArrayList<>();
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5NmI0NWY5MmNkYWNhZjY3NDFlNWVmMTA1MzY1MDkwNyIsIm5iZiI6MTczNjUzOTExMS4zNDUsInN1YiI6IjY3ODE3YmU3Mzg4MWM3OTQxOWJiNzcxNyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.yNCqvMpnCqFwGPCHxfoSA1sO_8boWww7SRYrpeEsWJ0"; // Reemplazar con la API key

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinnerGeneros = binding.genreSpinner;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new okhttp3.OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            okhttp3.Request request = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + API_KEY)
                                    .addHeader("accept", "application/json")
                                    .build();
                            return chain.proceed(request);
                        })
                        .build())
                .build();

        ApiService = retrofit.create(TMDBApiService.class);

        getGenres();

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.yearEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "El año no puede estar vacío", Toast.LENGTH_SHORT).show();
                } else {
                    String date = binding.yearEditText.getText().toString();
                    int selectedPosition = spinnerGeneros.getSelectedItemPosition();

                    if (selectedPosition != AdapterView.INVALID_POSITION && selectedPosition < generosList.size()) {
                        Genero selectedGenero = generosList.get(selectedPosition);

                        Intent intent = new Intent(getActivity(), MovieSearchResultActivity.class);

                        intent.putExtra("year", date);
                        intent.putExtra("genreId", selectedGenero.getId());
                        intent.putExtra("genreName", selectedGenero.getNombre());

                        startActivity(intent);

                    } else {
                        Toast.makeText(getContext(), "Seleccione un género válido", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return root;
    }

    private void getGenres() {
        Call<GeneroResponse> call = ApiService.getGenres("es-ES");
        call.enqueue(new Callback<GeneroResponse>() {
            @Override
            public void onResponse(Call<GeneroResponse> call, Response<GeneroResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generosList = response.body().getGenres();
                    List<String> generoNames = new ArrayList<>();
                    for (Genero genero : generosList) {
                        generoNames.add(genero.getNombre());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item,
                            generoNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerGeneros.setAdapter(adapter);

                } else {
                    Toast.makeText(getContext(), "Error al obtener los géneros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeneroResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error al conectar con la API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
