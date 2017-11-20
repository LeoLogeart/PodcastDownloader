package com.cynh.podcastdownloader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;

import com.cynh.podcastdownloader.R;
import com.cynh.podcastdownloader.context.DownloadActivity;
import com.cynh.podcastdownloader.model.Podcast;
import com.cynh.podcastdownloader.utils.Constants;
import com.cynh.podcastdownloader.utils.MediaBrowserManager;
import com.cynh.podcastdownloader.utils.Utils;


public class PodcastViewHolder extends RecyclerView.ViewHolder {
    private final CardView cardView;
    private final DownloadActivity context;
    private PodcastView podcastView;
    private boolean playing = false;
    private boolean expanded = false;
    private Handler myHandler = new Handler();
    private long startTime;
    private MediaBrowserManager mediaBrowserManager;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && context.getCurrentPlayerHolder() != null && mediaBrowserManager != null) {
                mediaBrowserManager.seekTo(progress);
                podcastView.setProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    private long progress;
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                podcastView.setProgress(progress + System.currentTimeMillis() - startTime);
                if (playing)
                    myHandler.postDelayed(this, Constants.UPDATE_DELAY_MILLIS);
            } catch (Exception ignored) {
            }
        }
    };
    private boolean animating = false;
    private final AnimatorListenerAdapter animatorEndListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            animating = false;
        }
    };
    private MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (state.getActions() == PlaybackStateCompat.ACTION_PAUSE) {
                        podcastView.setImagePlay();
                        playing = false;
                    } else if (state.getActions() == PlaybackStateCompat.ACTION_PLAY) {
                        myHandler.postDelayed(UpdateSongTime, Constants.UPDATE_DELAY_MILLIS);
                        podcastView.setImagePause();
                        playing = true;
                    } else if (state.getActions() == PlaybackStateCompat.ACTION_STOP) {
                        if (animating) {
                            return;
                        }
                        podcastView.setImagePlay();
                        playing = false;
                        if (mediaBrowserManager == null) {
                            //should not happen
                            return;
                        }
                        mediaBrowserManager.disconnect();
                        mediaBrowserManager = null;
                        podcastView.blockButtons();
                        podcastView.fadeOutPlayerButtons();
                        collapse();
                        podcastView.fadeInExpandButton();
                        return;
                    }
                    progress = state.getBufferedPosition();
                    startTime = System.currentTimeMillis();
                    podcastView.setProgress(state.getBufferedPosition());
                }
            };

    PodcastViewHolder(View podcastView, DownloadActivity context) {
        super(podcastView);
        cardView = itemView.findViewById(R.id.cv);
        this.podcastView = new PodcastView(podcastView, this, context);
        this.context = context;
    }

    void initHolder(Podcast podcast) {
        podcastView.initView(podcast);
    }

    void InitDownloadedHolder(Podcast podcast) {
        if (context.getPlayingUri().equals(Uri.parse(podcast.getUri()))) {
            setCurrentlyPlaying();
        } else {
            podcastView.initPlayingInfo();
        }
        podcastView.initClickListeners(v -> onPlayPauseClick(), seekBarChangeListener);
    }

    private void setCurrentlyPlaying() {
        context.setCurrentPlayerHolder(this);
        setExpandedCardviewNoAnimation();
        initHolderPlayer(context.getPlayingPodcast());
        if (playing) {
            podcastView.setImagePause();
        }
    }

    private void expand() {
        expanded = true;
        animating = true;
        podcastView.expand(animatorEndListenerAdapter);
    }

    private void collapse() {
        expanded = false;
        animating = true;
        podcastView.collapse(animatorEndListenerAdapter);
    }

    CardView getCardView() {
        return cardView;
    }

    private void setExpandedCardviewNoAnimation() {
        podcastView.enableButtons();
        expanded = true;
        podcastView.expandedCardviewNoAnimation();
    }

    private void setExpandedCardview() {
        if (animating) {
            return;
        }
        podcastView.enableButtons();
        expand();
        podcastView.fadeInPlayerButtons();
        podcastView.fadeOutExpandButton();
    }

    void OnDownloadedCardViewClick(Podcast podcast) {
        if (!expanded) {
            if (context.getCurrentPlayerHolder() != null && context.getCurrentPlayerHolder().isExpanded()) {
                context.getCurrentPlayerHolder().stopPlayer();
            }
            context.setPodcast(podcast);
            initHolderPlayer(podcast);
            podcastView.setImagePlay();
            setExpandedCardview();
        } else {
            stopPlayer();
        }
    }

    private void initHolderPlayer(Podcast podcast) {
        context.setCurrentPlayerHolder(this);
        progress = (new Utils(context)).getTime(podcast.getUri());
        podcastView.setProgressBarMax(podcast);
        podcastView.setProgress(progress);
        playing = false;
        mediaBrowserManager = context.connect();
    }

    private void stopPlayer() {
        playing = false;
        mediaBrowserManager.stop();
    }

    private void onPlayPauseClick() {
        if (mediaBrowserManager == null) {
            mediaBrowserManager = context.connect();
        }
        if (!playing) {
            mediaBrowserManager.play();
            podcastView.setImagePause();
        } else {
            mediaBrowserManager.pause();
            myHandler.removeCallbacks(UpdateSongTime);
            podcastView.setImagePlay();
        }
        playing = !playing;
    }

    private boolean isExpanded() {
        return expanded;
    }

    public MediaControllerCompat.Callback getControllerCallback() {
        return controllerCallback;
    }

    MediaBrowserManager getMediaBrowserManager() {
        return mediaBrowserManager;
    }
}
