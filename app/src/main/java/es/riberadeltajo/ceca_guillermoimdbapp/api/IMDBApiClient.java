package es.riberadeltajo.ceca_guillermoimdbapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IMDBApiClient {
    private static final String BASE_URL = "https://imdb-com.p.rapidapi.com/";
    private static IMDBApiService apiService;
    private static RapidApiKeyManager apiKeyManager = new RapidApiKeyManager();

    public static IMDBApiService getApiService() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(IMDBApiService.class);
        }
        return apiService;
    }

    public static String getApiKey() {
        return apiKeyManager.getCurrentKey();
    }

    public static void switchApiKey() {
        apiKeyManager.switchToNextKey();
    }
}
