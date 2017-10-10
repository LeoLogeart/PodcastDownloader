package com.dl.podcastgrossestetes;

import android.animation.ValueAnimator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

    PodcastViewHolder(View podcastView, DownloadActivity context) {
        super(podcastView);
        cardView = (CardView) itemView.findViewById(R.id.cv);
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
        this.context = context;
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


    void fadeOutPlayerButtons() {
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

    void fadeInExpandButton() {
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

    void collapse() {
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

    SeekBar getSeekBar() {
        return seekBar;
    }

    void setExpandButtonGone() {
        image_expand.setVisibility(View.GONE);
    }

    CardView getCardView() {
        return cardView;
    }

    boolean isExpanded() {
        return expanded;
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
    }

    void setImagePlay() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
    }

    boolean isPlaying() {
        return playing;
    }

    void setImagePause() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_img_selector));
    }

    void initPlayingInfo() {
        image_expand.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
    }

    void setExpandedCardviewNoAnimation() {
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

    void setExpandedCardview() {
        enableButtons();
        expand();
        fadeInPlayerButtons();
        fadeOutExpandButton();
    }

    void setPlayerTimeText(long progress) {
        playerTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(progress))));
    }

    View getImageViewPlayPause() {
        return image_play_pause;
    }

    View getImageViewBack() {
        return image_back;
    }

    View getImageViewForward() {
        return image_forward;
    }

    void blockButtons() {
        image_back.setClickable(false);
        image_forward.setClickable(false);
        image_play_pause.setClickable(false);
    }

    private void enableButtons() {
        image_back.setClickable(true);
        image_forward.setClickable(true);
        image_play_pause.setClickable(true);
    }
}
