package es.riberadeltajo.ceca_guillermoimdbapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Movie implements Parcelable {

    private String id;
    private String title;
    private String originalTitle;
    private String posterPath;
    private String releaseDate;
    private String descripcion;
    private String rating;


    public Movie() {
    }

    public Movie(String id, String title, String originalTitle, String imageUrl, String releaseDate) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.posterPath = imageUrl;
        this.releaseDate = releaseDate;
    }

    protected Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        originalTitle = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        descripcion = in.readString();
        rating = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public Movie(int id, String title, String posterPath) {
        this.id = String.valueOf(id);
        this.title = title;
        this.posterPath = posterPath;
    }

    public Movie(String id, String title, String posterPath) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(releaseDate);
        dest.writeString(descripcion);
        dest.writeString(rating);
    }

    public static class TitleText implements Parcelable {

        private String text;

        public TitleText() {}

        protected TitleText(Parcel in) {
            text = in.readString();
        }

        public static final Creator<TitleText> CREATOR = new Creator<TitleText>() {
            @Override
            public TitleText createFromParcel(Parcel in) {
                return new TitleText(in);
            }

            @Override
            public TitleText[] newArray(int size) {
                return new TitleText[size];
            }
        };

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
        }
    }

    public static class PrimaryImage implements Parcelable {

        private String url;

        public PrimaryImage() {}

        protected PrimaryImage(Parcel in) {
            url = in.readString();
        }

        public static final Creator<PrimaryImage> CREATOR = new Creator<PrimaryImage>() {
            @Override
            public PrimaryImage createFromParcel(Parcel in) {
                return new PrimaryImage(in);
            }

            @Override
            public PrimaryImage[] newArray(int size) {
                return new PrimaryImage[size];
            }
        };

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
        }
    }

    public static class MeterRanking implements Parcelable {
        private int currentRank;

        public MeterRanking() {}

        protected MeterRanking(Parcel in) {
            currentRank = in.readInt();
        }

        public static final Creator<MeterRanking> CREATOR = new Creator<MeterRanking>() {
            @Override
            public MeterRanking createFromParcel(Parcel in) {
                return new MeterRanking(in);
            }

            @Override
            public MeterRanking[] newArray(int size) {
                return new MeterRanking[size];
            }
        };

        public int getCurrentRank() {
            return currentRank;
        }

        public void setCurrentRank(int currentRank) {
            this.currentRank = currentRank;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(currentRank);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("releaseDate", releaseDate);
        map.put("rating", rating);
        map.put("posterUrl", posterPath);
        return map;
    }
}
