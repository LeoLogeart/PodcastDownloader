package com.dl.podcastgrossestetes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MediaPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.dl.podcastgrossestetes.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.dl.podcastgrossestetes.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.dl.podcastgrossestetes.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.dl.podcastgrossestetes.ACTION_NEXT";
    public static final String ACTION_STOP = "com.dl.podcastgrossestetes.ACTION_STOP";
    private static MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Podcast currentPodcast;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private NotificationCompat.Builder notificationBuilder;
    private PlaybackStatus playbackStatus;
    private long playbackState;
    private PlaybackStateCompat.Builder stateBuilder;
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    @Nullable
    @Override
    public MediaBrowserServiceCompat.BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        try {
            initMediaSession();
            if (rootHints != null) {
                currentPodcast = rootHints.getParcelable("media");
                initMediaPlayer();
                buildNotification(PlaybackStatus.PAUSED);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new BrowserRoot(getPackageName(), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        result.sendResult(mediaItems);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(currentPodcast.getUri());
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void pauseMedia() {
        buildNotification(PlaybackStatus.PAUSED);
        mediaPlayer.pause();
    }

    private void resumeMedia() {
        buildNotification(PlaybackStatus.PLAYING);
        if (!requestAudioFocus()) {
            stopSelf();
        }
        if (mediaPlayer == null) {
            initMediaPlayer();
        }
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("GrossesTetes", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("GrossesTetes", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("GrossesTetes", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //playMedia();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) initMediaPlayer();
                else mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pauseMedia();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return audioManager == null || AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            currentPodcast = intent.getExtras().getParcelable("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mediaSessionManager == null && mediaSession == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer = null;
        }
        removeAudioFocus();
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();
        unregisterReceiver(becomingNoisyReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        registerBecomingNoisyReceiver();
    }

    private void initMediaSession() throws RemoteException {
        Log.e("YAY", "initMediaSession");
        if (mediaSessionManager != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }

        mediaSession = new MediaSessionCompat(getApplicationContext(), "PGT");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        setSessionToken(mediaSession.getSessionToken());
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onStop() {
                super.onStop();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                updatePlaybackState(PlaybackStateCompat.ACTION_FAST_FORWARD);
                long previousState = playbackState;
                mediaPlayer.seekTo(Math.min(mediaPlayer.getCurrentPosition() + Constants.NEXT_PREVIOUS_OFFSET, mediaPlayer.getDuration()));
                updatePlaybackState(previousState);
                updateNotificationProgress();
                showNotification();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                updatePlaybackState(PlaybackStateCompat.ACTION_REWIND);
                long previousState = playbackState;
                mediaPlayer.seekTo(Math.max(0, mediaPlayer.getCurrentPosition() - Constants.NEXT_PREVIOUS_OFFSET));
                updatePlaybackState(previousState);
                updateNotificationProgress();
                showNotification();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                long previousState = playbackState;
                updatePlaybackState(PlaybackStateCompat.ACTION_SEEK_TO);
                mediaPlayer.seekTo((int) position);
                updatePlaybackState(previousState);
                updateNotificationProgress();
                showNotification();
            }
        });
    }


    private void buildNotification(PlaybackStatus playbackStatus) {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }
        this.playbackStatus = playbackStatus;
        int notificationAction = R.drawable.ic_pause_black_24dp;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            updatePlaybackState(PlaybackStateCompat.ACTION_PLAY);
            notificationAction = R.drawable.ic_pause_black_24dp;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            updatePlaybackState(PlaybackStateCompat.ACTION_PAUSE);
            notificationAction = R.drawable.ic_play_black_24dp;
            play_pauseAction = playbackAction(0);
        }
        Intent intent = new Intent(MediaPlayerService.this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MediaPlayerService.this, Constants.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        deleteIntent.setAction(ACTION_STOP);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(MediaPlayerService.this, 1, deleteIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                currentPodcast.getImage());

        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(currentPodcast.getDescription())
                .setContentTitle(currentPodcast.getDay())
                .addAction(R.drawable.ic_restore_black_24dp, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .setUsesChronometer(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingDeleteIntent)
                .addAction(R.drawable.ic_forward_black_24dp, "next", playbackAction(2));
        updateNotificationProgress();

        showNotification();
    }

    private void updatePlaybackState(long playbackstate) {
        this.playbackState = playbackstate;
        stateBuilder = new PlaybackStateCompat.Builder().setActions(playbackstate);
        if (playbackstate != PlaybackStateCompat.ACTION_STOP) {
            stateBuilder.setBufferedPosition(mediaPlayer.getCurrentPosition());
            Bundle bundle = new Bundle();
            bundle.putInt("duration", mediaPlayer.getDuration());
            stateBuilder.setExtras(bundle);
            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    private void showNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(Constants.NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationProgress() {
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationBuilder.setWhen(System.currentTimeMillis() - mediaPlayer.getCurrentPosition()).setShowWhen(true).setUsesChronometer(true);
            notificationBuilder.setContentInfo(null);
        } else {
            notificationBuilder.setWhen(0).setShowWhen(false).setUsesChronometer(false);
            int progress = mediaPlayer.getCurrentPosition();
            notificationBuilder.setContentInfo(String.format(Locale.FRANCE, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(progress),
                    TimeUnit.MILLISECONDS.toSeconds(progress) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(progress))));
        }

    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        updatePlaybackState(PlaybackStateCompat.ACTION_STOP);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        }
    }

    private enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

}