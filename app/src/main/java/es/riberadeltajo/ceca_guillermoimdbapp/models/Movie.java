package es.riberadeltajo.ceca_guillermoimdbapp.models;

public class Movie {
    private String title;
    private String imageUrl;

    // Constructor
    public Movie(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
