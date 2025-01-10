
package es.riberadeltajo.ceca_guillermoimdbapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.models.MovieOverviewResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class MovieDetailsActivity extends AppCompatActivity {

    private Movie pelicula;
    private TextView txt;
    private IMDBApiService imdbApiService;
    private TextView txt2;
    private ImageView imagen;
    private Button btnBackToTop10;
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int PICK_CONTACT_REQUEST = 1;
    private String selectedPhoneNumber;
    private String movieRating;  // Variable para almacenar el rating

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i = getIntent();
        pelicula = i.getParcelableExtra("pelicula");

        txt = findViewById(R.id.TextViewTitle);
        txt2 = findViewById(R.id.TextViewDescription);
        TextView releaseDateView = findViewById(R.id.TextViewDate);
        txt.setText(pelicula.getTitle());
        imagen = findViewById(R.id.ImageViewPortada);
        btnBackToTop10 = findViewById(R.id.btnBackToTop10);

        // Cargar la imagen del poster usando Glide
        Glide.with(this)
                .load(pelicula.getPosterPath())
                .into(imagen);

        // Configuración de OkHttpClient con encabezados para la API
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request modifiedRequest = chain.request().newBuilder()
                            .addHeader("X-RapidAPI-Key", "9c478d1de5msh0376a3e3aa6209ep161637jsn5be10011fc93")
                            .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                            .build();
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        // Inicialización de Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        imdbApiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = imdbApiService.obtenerDatos(pelicula.getId());
        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String descripcion = response.body().getData().getTitle().getPlot().getPlotText().getPlainText();
                    txt2.setText(descripcion);

                    // Obtener y formatear la fecha de lanzamiento
                    MovieOverviewResponse.ReleaseDate releaseDate = response.body().getData().getTitle().getReleaseDate();
                    if (releaseDate != null) {
                        String formattedDate = String.format("%d-%02d-%02d", releaseDate.getYear(), releaseDate.getMonth(), releaseDate.getDay());
                        releaseDateView.setText("Release Date: " + formattedDate);
                    }

                    // Obtener y mostrar el rating
                    MovieOverviewResponse.RatingsSummary ratingsSummary = response.body().getData().getTitle().getRatingsSummary();
                    if (ratingsSummary != null) {
                        movieRating = String.format("%.1f", ratingsSummary.getAggregateRating());  // Guardamos el rating
                        Log.d("Rating", "Rating obtenido: " + movieRating);  // Añadimos un log para verificar
                        TextView ratingView = findViewById(R.id.TextViewRating);
                        ratingView.setText("Rating: " + movieRating);
                    } else {
                        Log.d("Rating", "No se encontró rating");  // Log si el rating es null
                    }

                } else {
                    Log.d("API Response", "Respuesta vacía o error en la respuesta");
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });

        // Verificar permisos
        checkPermissions();

        // Configurar el botón para seleccionar un contacto y abrir SMS
        Button btnSendSms = findViewById(R.id.btnSendSms);
        btnSendSms.setOnClickListener(view -> {
            if (selectedPhoneNumber == null) {
                // Si no hay contacto seleccionado, abrir el selector de contactos
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            } else {
                openSmsApp();
            }
        });
        btnBackToTop10.setOnClickListener(v -> {
            // Regresar al HomeFragment o a la pantalla principal con el Top 10
            Intent intent = new Intent(MovieDetailsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Finaliza la actividad actual
        });
    }

    // Verificar permisos en tiempo de ejecución
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    // Método para abrir la app de SMS con el número y el mensaje prellenado
    private void openSmsApp() {
        String movieDetails = "Esta película te gustará: " + pelicula.getTitle() + "\nRating: " + movieRating;  // Usamos el rating guardado

        // Intent para abrir la aplicación de SMS con el mensaje prellenado
        if (selectedPhoneNumber != null) {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + selectedPhoneNumber));
            smsIntent.putExtra("sms_body", movieDetails);
            startActivity(smsIntent);
        }
    }

    // Manejo de la selección de contacto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                selectedPhoneNumber = cursor.getString(columnIndex);
                cursor.close();
                // Después de seleccionar el contacto, abrir la aplicación de SMS
                openSmsApp();
            }
        }
    }

    // Manejo de resultados de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos
                Log.d("Permissions", "Permisos concedidos");
            } else {
                // Permisos no concedidos
                Log.d("Permissions", "Permisos no concedidos");
            }
        }
    }
}
