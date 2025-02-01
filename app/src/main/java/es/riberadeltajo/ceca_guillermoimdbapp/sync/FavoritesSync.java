package es.riberadeltajo.ceca_guillermoimdbapp.sync;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class FavoritesSync {

    private final FavoritesDatabaseHelper databaseHelper;
    private final FavoritesManager favoritesManager;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FavoritesSync(Context context) {
        this.databaseHelper = FavoritesDatabaseHelper.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.favoritesManager = new FavoritesManager(context);
    }

    public void syncToFirestore() {
        new Thread(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Log.e("FavoritesSync", "No hay usuario autenticado. Sincronización abortada.");
                return;
            }
            String userId = user.getUid();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = null;

            try {
                cursor = db.rawQuery("SELECT * FROM " + FavoritesDatabaseHelper.TABLE_FAVORITES, null);
                while (cursor.moveToNext()) {
                    String movieId = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_TITLE));
                    String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE));
                    String rating = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_RATING));
                    String posterPath = cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_POSTER_PATH));

                    addFavoriteToFirestore(movieId, title, releaseDate, rating, posterPath, userId);
                }
            } catch (Exception e) {
                Log.e("FavoritesSync", "Error al sincronizar datos locales a Firestore", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }
        }).start();
    }

    public void syncFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.e("FavoritesSync", "No hay usuario autenticado. No se puede sincronizar.");
            return;
        }

        String userId = user.getUid();
        firestore.collection("favorites")
                .document(userId)
                .collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> new Thread(() -> {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    db.beginTransaction();
                    try {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String movieId = doc.getString("id");
                            String title = doc.getString("title");
                            String releaseDate = doc.getString("releaseDate");
                            String rating = doc.getString("rating");
                            String posterUrl = doc.getString("posterUrl");

                            if (movieId != null && title != null) {
                                Movie movie = new Movie(movieId, title, releaseDate, rating, posterUrl);
                                favoritesManager.addFavorite(movie, userId);
                            }
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                        db.close();
                    }
                }).start())
                .addOnFailureListener(e -> Log.e("FavoritesSync", "Error al sincronizar desde Firestore", e));
    }

    private void addFavoriteToFirestore(String movieId, String title, String releaseDate, String rating, String posterPath, String userId) {
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("id", movieId);
        movieData.put("title", title);
        movieData.put("releaseDate", releaseDate);
        movieData.put("rating", rating);
        movieData.put("posterUrl", posterPath);
        movieData.put("userID", userId);

        firestore.collection("favorites")
                .document(userId)
                .collection("movies")
                .document(movieId)
                .set(movieData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FavoritesSync", "Película añadida: " + title))
                .addOnFailureListener(e -> Log.e("FavoritesSync", "Error al añadir película: " + title, e));
    }
}
