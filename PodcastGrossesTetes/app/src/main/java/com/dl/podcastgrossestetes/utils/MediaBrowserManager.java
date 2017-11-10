package com.dl.podcastgrossestetes.utils;


import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.dl.podcastgrossestetes.context.DownloadActivity;
import com.dl.podcastgrossestetes.context.MediaPlayerService;
import com.dl.podcastgrossestetes.model.Podcast;

public class MediaBrowserManager {

    private final MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback holderCallback;

    public MediaBrowserManager(DownloadActivity context, Podcast podcast) {
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
                    mediaController.getTransportControls().prepare();

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

    public void disconnect() {
        mediaController.unregisterCallback(holderCallback);
        if(mediaBrowser.isConnected()){
            mediaBrowser.disconnect();
        }
    }

    public void connect() {
        mediaBrowser.connect();
    }

    public void play() {
        mediaController.getTransportControls().play();
    }

    public void pause() {
        mediaController.getTransportControls().pause();
    }

    public void stop() {
        mediaController.getTransportControls().stop();
    }

    public void seekTo(int progress) {
        mediaController.getTransportControls().seekTo(progress);
    }

    public void skipToNext() {
        mediaController.getTransportControls().skipToNext();
    }

    public void skipToPrevious() {
        mediaController.getTransportControls().skipToPrevious();
    }


    public void dispatchMediaButtonEvent(KeyEvent event) {
        mediaController.dispatchMediaButtonEvent(event);
    }
}