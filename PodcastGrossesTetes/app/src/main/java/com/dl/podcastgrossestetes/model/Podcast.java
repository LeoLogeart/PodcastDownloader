package com.dl.podcastgrossestetes.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Podcast implements Parcelable {
    public static final Creator<Podcast> CREATOR = new Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel in) {
            return new Podcast(in);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };
    private static String[] types;
    private String url;
    private String title;
    private String uri;
    private String subtitle;
    private int image;
    private int duration = 0;
    private String type;
    private Status status;

    public Podcast(String podcastTitle, String podcastSubtitle, int podcastImage, String podcastUrl, String podcastType, int podcastDuration) {
        initPodcast(podcastTitle, podcastSubtitle, podcastImage, podcastUrl, podcastType, podcastDuration);
    }

    protected Podcast(Parcel in) {
        url = in.readString();
        title = in.readString();
        uri = in.readString();
        subtitle = in.readString();
        image = in.readInt();
    }


    public Podcast(String podcastTitle, String podcastSubtitle, int podcastImage, String podcastUrl, String podcastType) {
        initPodcast(podcastTitle, podcastSubtitle, podcastImage, podcastUrl, podcastType, 0);
    }

    public static String[] getPodcastTypes() {
        return types;
    }

    public static void setPodcastTypes(String[] podcastTypes) {
        types = podcastTypes;
    }

    private void initPodcast(String podcastTitle, String podcastSubtitle, int podcastImage, String podcastUrl, String podcastType, int podcastDuration) {
        this.title = podcastTitle;
        subtitle = podcastSubtitle;
        url = podcastUrl;
        type = podcastType;
        image = podcastImage;
        status = Status.NONE;
        duration = podcastDuration;
    }

    /**
     * Gets the podcast title that will be printed on screen.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the podcast image that will be printed on screen.
     *
     * @return image
     */
    public int getImage() {
        return image;
    }

    /**
     * Gets the podcast subtitles that will be printed on screen.
     *
     * @return subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }


    /**
     * Gets the podcast type. Depending on the user's preferences regarding types,
     * podcasts with certain type will not appear on screen.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the podcast URL that will be used to download the podcast.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the podcast's duration. it will be printed on screen if showDuration returns true.
     *
     * @return duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the status regarding the download.
     *
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the podcast URI on the device. It will be used to play the podcast.
     *
     * @return uri
     */
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDownloading() {
        status = Status.DOWNLOADING;
    }

    public void setDownloaded() {
        status = Status.DOWNLOADED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(title);
        parcel.writeString(uri);
        parcel.writeString(subtitle);
        parcel.writeInt(image);
    }

    public enum Status {
        DOWNLOADING, NONE, DOWNLOADED
    }
}
