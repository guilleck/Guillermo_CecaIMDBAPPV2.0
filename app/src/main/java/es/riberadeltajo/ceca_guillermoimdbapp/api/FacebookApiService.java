package es.riberadeltajo.ceca_guillermoimdbapp.api;

import es.riberadeltajo.ceca_guillermoimdbapp.models.UserProfile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FacebookApiService {
    @GET("me")
    Call<UserProfile> getUserProfile(@Query("fields") String fields);
}