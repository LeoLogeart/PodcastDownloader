package com.dl.podcastgrossestetes;

import java.util.Observable;

public class MediaPlayerObservableObject extends Observable{
    private static MediaPlayerObservableObject instance = new MediaPlayerObservableObject();

    private MediaPlayerObservableObject() {
    }

    public static MediaPlayerObservableObject getInstance() {
        return instance;
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

}