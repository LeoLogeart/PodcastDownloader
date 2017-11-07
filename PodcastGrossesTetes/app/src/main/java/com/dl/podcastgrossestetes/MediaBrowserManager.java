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
    private MediaControllerCompat.Callback holderCallback;

    MediaBrowserManager(DownloadActivity context, Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("media", podcast);
        MediaBrowserCompat.ConnectionCallback mConnectionCallbacks = new MediaBrowserCompat.ConnectionCallback() {
            @Override
            public void onConnected() {
                try {
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    mediaController = new MediaControllerCompat(context, token);
                    holderCallback = context.getCurrentPlayerHolder().getControllerCallback();
                    mediaController.registerCallback(holderCallback);

                } catch (Exception ignored) {

                }
            }

            @Override
            public void onConnectionSuspended() {
                Log.d("PGT", "Connection Suspended");
                // The Service has crashed. Disable transport controls until it automatically reconnects
            }

            @Override
            public void onConnectionFailed() {
                Log.d("PGT", "Connection Failed");
                // The Service has refused our connection
            }
        };
        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, MediaPlayerService.class),
                mConnectionCallbacks,
                bundle);
    }

    void disconnect() {
        mediaController.unregisterCallback(holderCallback);
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