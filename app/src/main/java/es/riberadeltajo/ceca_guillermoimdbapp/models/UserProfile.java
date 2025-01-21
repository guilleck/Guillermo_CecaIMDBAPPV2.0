package es.riberadeltajo.ceca_guillermoimdbapp.models;

public class UserProfile {
    private String id;
    private String name;
    private String email;
    private Picture picture;

    // Getters y Setters

    public static class Picture {
        private Data data;

        // Getters y Setters

        public static class Data {
            private String url;

            // Getters y Setters
        }
    }
}
