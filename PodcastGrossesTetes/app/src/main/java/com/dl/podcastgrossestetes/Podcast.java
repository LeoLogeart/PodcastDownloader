package com.dl.podcastgrossestetes;


public class Podcast {
    private String url;
    private String day;
    private String uri;
    private String description;
    private int image;
    private Type type;
    private Status status;

    public Podcast(String title, String podcastUrl) {
        PodcastParser parser = new PodcastParser();
        day = parser.getDay(title);
        description = title;
        url = podcastUrl;
        type = parser.getType(title);
        image = parser.getImg(type);
        status = Status.NONE;
    }

    public String getDay() {
        return day;
    }

    public int getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }


    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public void setDownloading() {
        status = Status.DOWNLOADING;
    }

    public void setDownloaded() {
        status = Status.DOWNLOADED;
    }

    public Status getStatus() {
        return status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public enum Type {
        INTEGRALE, PEPITE, BEST_OF, INVITE_MYSTERE
    }

    public enum Status {
        DOWNLOADING, NONE, DOWNLOADED
    }
}
