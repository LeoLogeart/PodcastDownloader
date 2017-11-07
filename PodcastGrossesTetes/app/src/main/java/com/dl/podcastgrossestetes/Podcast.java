package com.dl.podcastgrossestetes;


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

    protected Podcast(Parcel in) {
        url = in.readString();
        day = in.readString();
        uri = in.readString();
        description = in.readString();
        image = in.readInt();
    }

    String getDay() {
        return day;
    }

    public int getImage() {
        return image;
    }

    String getDescription() {
        return description;
    }


    Type getType() {
        return type;
    }

    String getUrl() {
        return url;
    }

    void setDownloading() {
        status = Status.DOWNLOADING;
    }

    void setDownloaded() {
        status = Status.DOWNLOADED;
    }

    Status getStatus() {
        return status;
    }

    String getUri() {
        return uri;
    }

    void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(day);
        parcel.writeString(uri);
        parcel.writeString(description);
        parcel.writeInt(image);
    }

    enum Type {
        INTEGRALE, PEPITE, BEST_OF, INVITE_MYSTERE
    }

    enum Status {
        DOWNLOADING, NONE, DOWNLOADED
    }
}
