package com.dl.podcastgrossestetes;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.List;

class PodcastAdapter extends RecyclerView.Adapter<PodcastViewHolder> {
    private final List<Podcast> podcasts;
    private boolean animating = false;
    private DownloadActivity context;
    private Handler myHandler = new Handler();
    private MediaPlayer mediaPlayer;

    private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
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
            try {
                int startTime = mediaPlayer.getCurrentPosition();
                UpdatePlayerTime(startTime, context.getCurrentPlayerHolder().getSeekBar());
                if (mediaPlayer.isPlaying())
                    myHandler.postDelayed(this, Constants.UPDATE_DELAY_MILLIS);
            } catch (Exception ignored) {}
        }
    };

    PodcastAdapter(List<Podcast> items, DownloadActivity ctx) {
        this.podcasts = items;
        context = ctx;
    }

    @Override
    public PodcastViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.print_item, viewGroup, false);
        return new PodcastViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(PodcastViewHolder holder, int position) {
        holder.initHolder(podcasts.get(position));

        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
            InitDownloadedHolder(holder, position);
        } else {
            holder.setExpandButtonGone();
        }

        holder.getCardView().setOnClickListener(v -> {
            if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
                if(animating)
                {
                    return;
                }
                animating = true;
                OnDownloadedCardViewClick(holder, position);
                final Handler handler = new Handler();
                handler.postDelayed(() -> animating = false, Constants.ANIMATION_DURATION);

            } else {
                (new LayoutUpdater(context)).createDownloadConfirmationDialog(podcasts.get(position));
            }
        });
    }

    private void OnDownloadedCardViewClick(PodcastViewHolder holder, int position) {
        if (!holder.isExpanded()) {
            if (context.getCurrentPlayerHolder() != null) {
                setCollapsedCardView(context.getCurrentPlayerHolder());
            }
            mediaPlayer = context.getMediaPlayer(Uri.parse(podcasts.get(position).getUri()));
            holder.getSeekBar().setMax(mediaPlayer.getDuration());
            context.setCurrentPlayerHolder(holder);
            int progress = context.getPreferences(Context.MODE_PRIVATE).getInt(context.getPlayingUri().toString(),0);
            mediaPlayer.seekTo(progress);
            UpdatePlayerTime(progress,holder.getSeekBar());
            holder.setExpandedCardview();
        } else {
            setCollapsedCardView(holder);
        }
    }

    private void setCollapsedCardView(PodcastViewHolder holder) {
        holder.blockButtons();
        StopAndSavePlayer();
        holder.fadeOutPlayerButtons();
        holder.collapse();
        holder.fadeInExpandButton();
    }

    private void StopAndSavePlayer() {
        saveTime(mediaPlayer.getCurrentPosition(), context.getPlayingUri());
        mediaPlayer.stop();
        myHandler.removeCallbacks(UpdateSongTime);
        context.getCurrentPlayerHolder().setPlaying(false);
        context.getCurrentPlayerHolder().setImagePlay();
        context.setCurrentPlayerHolder(null);
    }

    private void saveTime(int currentPosition, Uri playingUri) {
        SharedPreferences.Editor editor = context.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putInt(playingUri.toString(),currentPosition);
        editor.apply();
    }

    private void InitDownloadedHolder(PodcastViewHolder holder, int position) {
        mediaPlayer = context.getMediaPlayer();
        if (mediaPlayer != null && context.getPlayingUri().equals(Uri.parse(podcasts.get(position).getUri()))) {
            holder.setPlaying(mediaPlayer.isPlaying());
            holder.getSeekBar().setMax(mediaPlayer.getDuration());
            context.setCurrentPlayerHolder(holder);
            holder.setExpandedCardviewNoAnimation();
            if (holder.isPlaying()) {
                holder.setImagePause();
            }
            UpdatePlayerTime(mediaPlayer.getCurrentPosition(), holder.getSeekBar());
            myHandler.postDelayed(UpdateSongTime, Constants.UPDATE_DELAY_MILLIS);
        } else {
            holder.initPlayingInfo();
        }

        holder.getImageViewPlayPause().setOnClickListener(view -> onPlayPauseClick(holder));

        holder.getImageViewBack().setOnClickListener(view -> {
            mediaPlayer.seekTo(Math.max(mediaPlayer.getCurrentPosition() - 10000, 0));
            UpdatePlayerTime(mediaPlayer.getCurrentPosition(), holder.getSeekBar());
        });
        holder.getImageViewForward().setOnClickListener(view -> mediaPlayer.seekTo(Math.min(mediaPlayer.getCurrentPosition() + 10000, mediaPlayer.getDuration())));
        holder.getSeekBar().setOnSeekBarChangeListener(seekBarChangeListener);
    }
    private void onPlayPauseClick(PodcastViewHolder holder) {
        if (!holder.isPlaying()) {
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, Constants.UPDATE_DELAY_MILLIS);
            holder.setImagePause();
        } else {
            mediaPlayer.pause();
            myHandler.removeCallbacks(UpdateSongTime);
            holder.setImagePlay();
        }
        holder.setPlaying(!holder.isPlaying());
    }


    private void UpdatePlayerTime(long progress, SeekBar seekBar) {
        seekBar.setProgress((int) progress);
        context.getCurrentPlayerHolder().setPlayerTimeText(progress);
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

}

