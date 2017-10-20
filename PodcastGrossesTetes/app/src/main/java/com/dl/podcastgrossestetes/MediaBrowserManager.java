package com.dl.podcastgrossestetes;


import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

class MediaBrowserManager {

    private final MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    MediaBrowserManager(DownloadActivity context, Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("media", podcast);
        MediaBrowserCompat.ConnectionCallback mConnectionCallbacks = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    // Create a MediaControllerCompat
                    mediaController = new MediaControllerCompat(context, token);
                    //play();
                    mediaController.registerCallback(context.getCurrentPlayerHolder().controllerCallback);
                    //TODO buildTransportControls();

                } catch (Exception ignored) {

                }
            }

            @Override
            public void onConnectionSuspended() {
                Log.e("YAY", "onConnectionSuspended");
                // The Service has crashed. Disable transport controls until it automatically reconnects
            }

            @Override
            public void onConnectionFailed() {
                Log.e("YAY", "onConnectionFailed");
                // The Service has refused our connection
            }
        };
        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, MediaPlayerService.class),
                mConnectionCallbacks,
                bundle);
    }

    void disconnect() {
        mediaBrowser.disconnect();
    }

    void connect() {
        mediaBrowser.connect();
    }

    void play() {
        mediaController.getTransportControls().play();
    }

    void pause() {
        mediaController.getTransportControls().pause();
    }

    void stop() {
        mediaController.getTransportControls().stop();
    }

    void seekTo(int progress) {
        mediaController.getTransportControls().seekTo(progress);
    }

    void skipToNext() {
        mediaController.getTransportControls().skipToNext();
    }

    void skipToPrevious() {
        mediaController.getTransportControls().skipToPrevious();
    }

}