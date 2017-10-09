package com.dl.podcastgrossestetes;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {
    public static final int UPDATE_DELAY_MILLIS = 100;
    private static final int DURATION_MILLIS_FADE_IN = 500;
    private static final int START_OFFSET_FADE = 500;
    private static final int EXPAND_DURATION = 500;
    private final List<Podcast> podcasts;
    private DownloadActivity context;
    private Handler myHandler = new Handler();
    private MediaPlayer mediaPlayer;
    private PodcastViewHolder currentPlayerHolder;

    OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaPlayer.seekTo(progress);
                UpdatePlayerTime(progress, seekBar);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            int startTime = mediaPlayer.getCurrentPosition();
            UpdatePlayerTime(startTime, currentPlayerHolder.seekBar);
            currentPlayerHolder.seekBar.setProgress(startTime);
            myHandler.postDelayed(this, UPDATE_DELAY_MILLIS);
        }
    };

    public PodcastAdapter(List<Podcast> items, DownloadActivity ctx) {
        this.podcasts = items;
        context = ctx;
    }

    @Override
    public PodcastViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.print_item, viewGroup, false);
        PodcastViewHolder pvh = new PodcastViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PodcastViewHolder holder, int position) {
        initHolder(holder, position);

        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
            InitDownloadedHolder(holder, position);
        } else {
            holder.image_expand.setVisibility(View.GONE);
        }

        holder.cv.setOnClickListener(v -> {
            if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
                OnDownloadedCardViewClick(holder, position);
            } else {
                (new LayoutUpdater(context)).createDownloadConfirmationDialog(podcasts.get(position));
            }
        });
    }

    private void OnDownloadedCardViewClick(PodcastViewHolder holder, int position) {
        if (!holder.expanded) {
            if (currentPlayerHolder != null) {
                setCollapsedCardView(currentPlayerHolder);
            }
            mediaPlayer = context.getMediaPlayer(Uri.parse(podcasts.get(position).getUri()));
            holder.seekBar.setMax(mediaPlayer.getDuration());
            currentPlayerHolder = holder;
            int progress = context.getPreferences(Context.MODE_PRIVATE).getInt(context.getPlayingUri().toString(),0);
            mediaPlayer.seekTo(progress);
            UpdatePlayerTime(progress,holder.seekBar);
            setExpandedCardview(holder);
        } else {
            setCollapsedCardView(holder);
        }
    }

    private void setCollapsedCardView(PodcastViewHolder holder) {
        StopAndSavePlayer();
        holder.fadeOutPlayerButtons();
        holder.collapse();
        holder.fadeInExpandButton();
    }

    private void StopAndSavePlayer() {
        saveTime(mediaPlayer.getCurrentPosition(), context.getPlayingUri());
        mediaPlayer.stop();
        myHandler.removeCallbacks(UpdateSongTime);
        currentPlayerHolder.playing = false;
        currentPlayerHolder.image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
        currentPlayerHolder = null;
    }

    private void saveTime(int currentPosition, Uri playingUri) {
        SharedPreferences.Editor editor = context.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putInt(playingUri.toString(),currentPosition);
        editor.apply();
    }

    private void InitDownloadedHolder(PodcastViewHolder holder, int position) {
        mediaPlayer = context.getMediaPlayer();
        if (mediaPlayer != null && context.getPlayingUri().equals(Uri.parse(podcasts.get(position).getUri()))) {
            holder.playing = mediaPlayer.isPlaying();
            holder.seekBar.setMax(mediaPlayer.getDuration());
            currentPlayerHolder = holder;
            setExpandedCardviewNoAnimation(holder);
            if (holder.playing) {
                holder.image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_img_selector));
            }
            UpdatePlayerTime(mediaPlayer.getCurrentPosition(), holder.seekBar);
            myHandler.postDelayed(UpdateSongTime, UPDATE_DELAY_MILLIS);
        } else {
            holder.image_expand.setVisibility(View.VISIBLE);
            holder.seekBar.setProgress(0);
        }

        holder.image_play_pause.setOnClickListener(view -> InitDownloadedImagePlayPause(holder));

        holder.image_back.setOnClickListener(view -> {
            mediaPlayer.seekTo(Math.max(mediaPlayer.getCurrentPosition() - 10000, 0));
            UpdatePlayerTime(mediaPlayer.getCurrentPosition(), holder.seekBar);
        });
        holder.image_forward.setOnClickListener(view -> mediaPlayer.seekTo(Math.min(mediaPlayer.getCurrentPosition() + 10000, mediaPlayer.getDuration())));
        holder.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }
    private void InitDownloadedImagePlayPause(PodcastViewHolder holder) {
        if (!holder.playing) {
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, UPDATE_DELAY_MILLIS);
            holder.image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_img_selector));
        } else {
            mediaPlayer.pause();
            myHandler.removeCallbacks(UpdateSongTime);
            holder.image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
        }
        holder.playing = !holder.playing;
    }


    private void setExpandedCardviewNoAnimation(PodcastViewHolder holder) {
        holder.expanded = true;
        holder.player.setVisibility(View.VISIBLE);
        holder.image_forward.setVisibility(View.VISIBLE);
        holder.image_back.setVisibility(View.VISIBLE);
        holder.image_play_pause.setVisibility(View.VISIBLE);
        holder.seekBar.setVisibility(View.VISIBLE);
        holder.playerTime.setVisibility(View.VISIBLE);
        holder.image_expand.setVisibility(View.GONE);
    }

    private void setExpandedCardview(PodcastViewHolder holder) {
        holder.expand();
        holder.fadeInPlayerButtons();
        holder.fadeOutExpandButton();
    }

    private void initHolder(PodcastViewHolder holder, int position) {
        holder.image.setImageDrawable(ContextCompat.getDrawable(context, podcasts.get(position).getImage()));
        holder.title.setText(podcasts.get(position).getDay());
        holder.description.setText(podcasts.get(position).getDescription());
        holder.player.setVisibility(View.GONE);

        holder.image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
        holder.image_back.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.back_img_selector));

        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADING)) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }
    }

    private void UpdatePlayerTime(long progress, SeekBar seekBar) {
        seekBar.setProgress((int) progress);
        currentPlayerHolder.playerTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(progress)))
        );
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public static class PodcastViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image_play_pause;
        private final CardView cv;
        private final TextView title;
        private final TextView description;
        private final TextView playerTime;
        private final ImageView image;
        private final ImageView image_expand;
        private final ImageView image_forward;
        private final ProgressBar progressBar;
        private final ImageView image_back;
        private final ConstraintLayout player;
        private final SeekBar seekBar;
        private boolean playing = false;
        private boolean expanded = false;

        PodcastViewHolder(View podcastView) {
            super(podcastView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            image = (ImageView) podcastView.findViewById(R.id.img);
            image_expand = (ImageView) podcastView.findViewById(R.id.img_play);
            title = (TextView) podcastView.findViewById(R.id.title);
            playerTime = (TextView) podcastView.findViewById(R.id.player_time);
            description = (TextView) podcastView.findViewById(R.id.description);
            progressBar = (ProgressBar) podcastView.findViewById(R.id.progressBar);
            image_forward = (ImageView) podcastView.findViewById(R.id.img_forward);
            image_play_pause = (ImageView) podcastView.findViewById(R.id.img_play_pause);
            image_back = (ImageView) podcastView.findViewById(R.id.img_back);
            player = (ConstraintLayout) podcastView.findViewById(R.id.player);
            seekBar = (SeekBar) podcastView.findViewById(R.id.seekBar);
        }

        public void fadeInPlayerButtons() {
            Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(DURATION_MILLIS_FADE_IN);
            fadeIn.setStartOffset(START_OFFSET_FADE);
            image_forward.setAnimation(fadeIn);
            image_back.setAnimation(fadeIn);
            image_play_pause.setAnimation(fadeIn);
            seekBar.setAnimation(fadeIn);
            playerTime.setAnimation(fadeIn);
            fadeIn.start();
        }


        public void fadeOutPlayerButtons() {
            Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(DURATION_MILLIS_FADE_IN);
            fadeOut.setFillAfter(true);
            image_forward.startAnimation(fadeOut);
            image_back.startAnimation(fadeOut);
            image_play_pause.startAnimation(fadeOut);
            seekBar.startAnimation(fadeOut);
            playerTime.startAnimation(fadeOut);
        }

        public void fadeOutExpandButton() {
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
            mAnimator.setDuration(EXPAND_DURATION);
            mAnimator.start();
        }

        public void fadeInExpandButton() {
            ValueAnimator mAnimator = ValueAnimator.ofInt(0, 180);

            image_expand.setVisibility(View.VISIBLE);
            mAnimator.addUpdateListener(valueAnimator -> {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = image_expand.getLayoutParams();
                layoutParams.height = value;
                image_expand.setLayoutParams(layoutParams);
            });
            mAnimator.setDuration(EXPAND_DURATION);
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
            mAnimator.setDuration(EXPAND_DURATION);
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
            mAnimator.setStartDelay(START_OFFSET_FADE);
            mAnimator.setDuration(EXPAND_DURATION);
            mAnimator.start();
        }
    }
}

