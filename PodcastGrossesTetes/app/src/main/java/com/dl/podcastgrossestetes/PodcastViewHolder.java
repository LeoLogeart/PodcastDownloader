package com.dl.podcastgrossestetes;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


class PodcastViewHolder extends RecyclerView.ViewHolder {
    private final CardView cardView;
    private final TextView title;
    private final TextView description;
    private final TextView playerTime;
    private final ImageView image;
    private final ImageView image_play_pause;
    private final ImageView image_expand;
    private final ImageView image_forward;
    private final ImageView image_back;
    private final ProgressBar progressBar;
    private final ConstraintLayout player;
    private final SeekBar seekBar;
    private final DownloadActivity context;
    private boolean playing = false;
    private boolean expanded = false;
    private Handler myHandler = new Handler();
    private long startTime;
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            try {
                UpdatePlayerTime(progress+System.currentTimeMillis()-startTime, context.getCurrentPlayerHolder().getSeekBar());
                if (playing)
                    myHandler.postDelayed(this, Constants.UPDATE_DELAY_MILLIS);
            } catch (Exception ignored) {
            }
        }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && context.getCurrentPlayerHolder()!=null) {
                mediaBrowserManager.seekTo(progress);
                UpdatePlayerTime(progress, seekBar);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    private MediaBrowserManager mediaBrowserManager;
    private long progress;

    PodcastViewHolder(View podcastView, DownloadActivity context) {
        super(podcastView);
        cardView = itemView.findViewById(R.id.cv);
        image = podcastView.findViewById(R.id.img);
        image_expand = podcastView.findViewById(R.id.img_play);
        title = podcastView.findViewById(R.id.title);
        playerTime = podcastView.findViewById(R.id.player_time);
        description = podcastView.findViewById(R.id.description);
        progressBar = podcastView.findViewById(R.id.progressBar);
        image_forward = podcastView.findViewById(R.id.img_forward);
        image_play_pause = podcastView.findViewById(R.id.img_play_pause);
        image_back = podcastView.findViewById(R.id.img_back);
        player = podcastView.findViewById(R.id.player);
        seekBar = podcastView.findViewById(R.id.seekBar);
        this.context = context;
    }

    void initHolder(Podcast podcast) {
        image.setImageDrawable(ContextCompat.getDrawable(context, podcast.getImage()));
        title.setText(podcast.getDay());
        description.setText(podcast.getDescription());
        player.setVisibility(View.GONE);

        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
        image_back.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.back_img_selector));

        if (podcast.getStatus().equals(Podcast.Status.DOWNLOADING)) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    void InitDownloadedHolder(Podcast podcast) {
        if (mediaBrowserManager != null && context.getPlayingUri().equals(Uri.parse(podcast.getUri()))) {
            //TODO setCurrentlyPlaying(mediaPlayer);
        } else {
            initPlayingInfo();
        }

        image_play_pause.setOnClickListener(view -> onPlayPauseClick());

        image_back.setOnClickListener(view -> {
            mediaBrowserManager.skipToPrevious();
            //TODO UpdatePlayerTime(context.getMediaPlayer().getCurrentPosition(), seekBar);
        });
        image_forward.setOnClickListener(view -> {
            mediaBrowserManager.skipToNext();
            //TODO UpdatePlayerTime(context.getMediaPlayer().getCurrentPosition(), seekBar);
        });
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void setCurrentlyPlaying(MediaPlayer mediaPlayer) {
        setPlaying(mediaPlayer.isPlaying());
        seekBar.setMax(mediaPlayer.getDuration());
        context.setCurrentPlayerHolder(this);
        setExpandedCardviewNoAnimation();
        if (playing) {
            setImagePause();
        }
        UpdatePlayerTime(mediaPlayer.getCurrentPosition(), seekBar);
        myHandler.postDelayed(UpdateSongTime, Constants.UPDATE_DELAY_MILLIS);
    }

    private void fadeInPlayerButtons() {
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(Constants.DURATION_MILLIS_FADE);
        fadeIn.setStartOffset(Constants.START_OFFSET_FADE);
        image_forward.setAnimation(fadeIn);
        image_back.setAnimation(fadeIn);
        image_play_pause.setAnimation(fadeIn);
        seekBar.setAnimation(fadeIn);
        playerTime.setAnimation(fadeIn);
        fadeIn.start();
    }

    private void fadeOutPlayerButtons() {
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(Constants.DURATION_MILLIS_FADE);
        fadeOut.setFillAfter(true);
        image_forward.startAnimation(fadeOut);
        image_back.startAnimation(fadeOut);
        image_play_pause.startAnimation(fadeOut);
        seekBar.startAnimation(fadeOut);
        playerTime.startAnimation(fadeOut);
    }

    private void fadeOutExpandButton() {
        ValueAnimator mAnimator = ValueAnimator.ofInt(image_expand.getHeight(), 0);

        mAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = image_expand.getLayoutParams();
            layoutParams.height = value;
            image_expand.setLayoutParams(layoutParams);
            if (value == 0) {
                image_expand.setVisibility(View.GONE);
            }
        });
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.start();
    }

    private void fadeInExpandButton() {
        ValueAnimator mAnimator = ValueAnimator.ofInt(0, 180);

        image_expand.setVisibility(View.VISIBLE);
        mAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = image_expand.getLayoutParams();
            layoutParams.height = value;
            image_expand.setLayoutParams(layoutParams);
        });
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.start();
    }

    private void expand() {
        expanded = true;
        player.setVisibility(View.VISIBLE);
        ValueAnimator mAnimator = ValueAnimator.ofInt(0, 180);

        mAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = player.getLayoutParams();
            layoutParams.height = value;
            player.setLayoutParams(layoutParams);
        });
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.start();
    }

    private void collapse() {
        expanded = false;
        ValueAnimator mAnimator = ValueAnimator.ofInt(player.getHeight(), 0);

        mAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = player.getLayoutParams();
            layoutParams.height = value;
            player.setLayoutParams(layoutParams);
        });
        mAnimator.setStartDelay(Constants.START_OFFSET_EXPAND);
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.start();
    }

    void setExpandButtonGone() {
        image_expand.setVisibility(View.GONE);
    }

    CardView getCardView() {
        return cardView;
    }

    private void setImagePlay() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
    }

    private void setImagePause() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_img_selector));
    }

    private void initPlayingInfo() {
        image_expand.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
    }

    private void setExpandedCardviewNoAnimation() {
        enableButtons();
        expanded = true;
        player.setVisibility(View.VISIBLE);
        image_forward.setVisibility(View.VISIBLE);
        image_back.setVisibility(View.VISIBLE);
        image_play_pause.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        playerTime.setVisibility(View.VISIBLE);
        image_expand.setVisibility(View.GONE);
    }

    private void setExpandedCardview() {
        enableButtons();
        expand();
        fadeInPlayerButtons();
        fadeOutExpandButton();
    }

    private void setPlayerTimeText(long progress) {
        playerTime.setText(String.format(Locale.FRANCE,"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(progress))));
    }

    private void blockButtons() {
        image_back.setClickable(false);
        image_forward.setClickable(false);
        image_play_pause.setClickable(false);
    }

    private void enableButtons() {
        image_back.setClickable(true);
        image_forward.setClickable(true);
        image_play_pause.setClickable(true);
    }

    void OnDownloadedCardViewClick(Podcast podcast) {
        if (!expanded) {
            if (context.getCurrentPlayerHolder() != null) {
                context.getCurrentPlayerHolder().stopAndSavePlayer();
            }
            context.setPodcast(podcast);
            context.setCurrentPlayerHolder(this);
            int progress = context.getPreferences(Context.MODE_PRIVATE).getInt(context.getPlayingUri().toString(), 0);
            UpdatePlayerTime(progress, seekBar);
            if(mediaBrowserManager==null)
            {
                mediaBrowserManager = context.connect();
            }
            setExpandedCardview();
        } else {
            setCollapsedCardView();
        }
    }

    private void setCollapsedCardView() {
        blockButtons();
        stopAndSavePlayer();
        fadeOutPlayerButtons();
        collapse();
        fadeInExpandButton();
    }

    void stopAndSavePlayer() {
        /*(new Utils(context)).saveTime(context.getMediaPlayer().getCurrentPosition(), context.getPlayingUri());
        context.getMediaPlayer().stop();*/
        mediaBrowserManager.stop();
        context.setCurrentPlayerHolder(null);
    }

    void onPlayPauseClick() {
        if(mediaBrowserManager==null)
        {
            mediaBrowserManager = context.connect();
        }
        if (!playing) {
            mediaBrowserManager.play();
            setImagePause();
        } else {
            mediaBrowserManager.pause();
            myHandler.removeCallbacks(UpdateSongTime);
            setImagePlay();
            /*(new Utils(context)).saveTime(context.getMediaPlayer().getCurrentPosition(),context.getPlayingUri());*/
        }
        playing = !playing;
    }


    private void UpdatePlayerTime(long progress, SeekBar seekBar) {
        Log.e("YAY","UpdatePlayerTime "+progress);
        seekBar.setProgress((int) progress);
        context.getCurrentPlayerHolder().setPlayerTimeText(progress);
    }

    private void setPlaying(boolean playing) {
        this.playing = playing;
    }

    private SeekBar getSeekBar() {
        return seekBar;
    }

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if(state.getActions()==PlaybackStateCompat.ACTION_PAUSE){
                        setImagePlay();
                        playing=false;
                    } else if(state.getActions()==PlaybackStateCompat.ACTION_PLAY){
                        myHandler.postDelayed(UpdateSongTime, Constants.UPDATE_DELAY_MILLIS);
                        setImagePause();
                        playing=true;
                    } else if(state.getActions()==PlaybackStateCompat.ACTION_STOP)
                    {
                        setImagePlay();
                        playing=false;
                        context.disconnect();
                        mediaBrowserManager = null;
                        blockButtons();
                        fadeOutPlayerButtons();
                        collapse();
                        fadeInExpandButton();
                        return;
                    }
                    seekBar.setMax( state.getExtras().getInt("duration"));
                    progress = state.getBufferedPosition();
                    startTime = System.currentTimeMillis();
                    UpdatePlayerTime( state.getBufferedPosition(), seekBar);
                }
            };
}