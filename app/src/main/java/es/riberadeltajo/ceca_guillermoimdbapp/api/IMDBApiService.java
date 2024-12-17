package es.riberadeltajo.ceca_guillermoimdbapp.api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class IMDBApiService {
    private static final String BASE_URL = "https://imdb-com.p.rapidapi.com/title/get-top-meter";
    private static final String API_KEY = "e7d5b81033mshd35b173690b52afp1dc062jsn34301e6f1bca";
    private static final String API_HOST = "imdb-com.p.rapidapi.com";

    private final OkHttpClient client;

    public IMDBApiService() {
        client = new OkHttpClient();
    }

    public void getTopMeterTitles(String topMeterTitlesType, final IMDBApiCallback callback) {
        // Construir la URL de la solicitud
        String url = BASE_URL + "?topMeterTitlesType=" + topMeterTitlesType;

        // Construir la solicitud HTTP con encabezados
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", API_HOST)
                .build();

        // Ejecutar la solicitud de forma asíncrona
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejar fallo en la llamada
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    callback.onSuccess(responseData);
                } else {
                    callback.onError("Error en la respuesta: " + response.code());
                }
            }
        });
    }

    // Interfaz de Callback
    public interface IMDBApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
