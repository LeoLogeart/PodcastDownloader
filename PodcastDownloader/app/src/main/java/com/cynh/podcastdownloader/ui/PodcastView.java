package com.cynh.podcastdownloader.ui;


import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cynh.podcastdownloader.R;
import com.cynh.podcastdownloader.model.Podcast;
import com.cynh.podcastdownloader.utils.Constants;
import com.cynh.podcastdownloader.utils.Utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


class PodcastView {

    private final TextView title;
    private final TextView description;
    private final TextView playerTime;
    private final TextView duration;
    private final ImageView image;
    private final ImageView image_play_pause;
    private final ImageView image_expand;
    private final ImageView image_forward;
    private final ImageView image_back;
    private final ProgressBar progressBar;
    private final ConstraintLayout player;
    private final SeekBar seekBar;
    private final Context context;
    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener;
    private final PodcastViewHolder holder;

    PodcastView(View podcastView, PodcastViewHolder holder, Context ctx) {
        context = ctx;
        this.holder = holder;
        image = podcastView.findViewById(R.id.img);
        image_expand = podcastView.findViewById(R.id.img_play);
        title = podcastView.findViewById(R.id.title);
        playerTime = podcastView.findViewById(R.id.player_time);
        description = podcastView.findViewById(R.id.description);
        progressBar = podcastView.findViewById(R.id.progressBar);
        image_forward = podcastView.findViewById(R.id.img_forward);
        image_play_pause = podcastView.findViewById(R.id.img_play_pause);
        image_back = podcastView.findViewById(R.id.img_back);
        duration = podcastView.findViewById(R.id.duration);
        player = podcastView.findViewById(R.id.player);
        seekBar = podcastView.findViewById(R.id.seekBar);
        animatorUpdateListener = valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = player.getLayoutParams();
            layoutParams.height = value;
            player.setLayoutParams(layoutParams);
        };
    }


    void initView(Podcast podcast) {
        image.setImageDrawable(ContextCompat.getDrawable(context, podcast.getImage()));
        title.setText(podcast.getTitle());
        description.setText(podcast.getSubtitle());
        player.setVisibility(View.GONE);
        image_expand.setVisibility(View.GONE);
        if (podcast.getDuration() != 0) {
            long podcastLength = podcast.getDuration();
            duration.setText(String.format(Locale.FRANCE, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(podcastLength),
                    TimeUnit.MILLISECONDS.toSeconds(podcastLength) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes(podcastLength))));
            duration.setVisibility(View.VISIBLE);
        }

        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
        image_back.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.back_img_selector));

        if (podcast.getStatus().equals(Podcast.Status.DOWNLOADING)) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    void fadeInPlayerButtons() {
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

    void setImagePlay() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_img_selector));
    }

    void expandedCardviewNoAnimation() {
        player.setVisibility(View.VISIBLE);
        image_forward.setVisibility(View.VISIBLE);
        image_back.setVisibility(View.VISIBLE);
        image_play_pause.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        playerTime.setVisibility(View.VISIBLE);
        image_expand.setVisibility(View.GONE);
        duration.setVisibility(View.GONE);
    }

    void blockButtons() {
        image_back.setClickable(false);
        image_forward.setClickable(false);
        image_play_pause.setClickable(false);
    }

    void enableButtons() {
        image_back.setClickable(true);
        image_forward.setClickable(true);
        image_play_pause.setClickable(true);
    }

    void setProgress(long progress) {
        seekBar.setProgress((int) progress);
        playerTime.setText(String.format(Locale.FRANCE, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress),
                TimeUnit.MILLISECONDS.toSeconds(progress) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes(progress))));
    }

    void setImagePause() {
        image_play_pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_img_selector));
    }

    void initPlayingInfo() {
        image_expand.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
    }

    void initClickListeners(View.OnClickListener playPauseListener, SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        image_play_pause.setOnClickListener(playPauseListener);

        image_back.setOnClickListener(view -> holder.getMediaBrowserManager().skipToPrevious());
        image_forward.setOnClickListener(view -> holder.getMediaBrowserManager().skipToNext());
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    void fadeOutExpandButton() {
        AnimatorSet set = new AnimatorSet();
        ValueAnimator buttonAnimator = ValueAnimator.ofInt(image_expand.getHeight(), 0);

        buttonAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = image_expand.getLayoutParams();
            layoutParams.height = value;
            image_expand.setLayoutParams(layoutParams);
            if (value == 0) {
                image_expand.setVisibility(View.GONE);
            }
        });
        buttonAnimator.setDuration(Constants.EXPAND_DURATION);
        if (duration.getText() != null && !duration.getText().equals("")) {
            duration.startAnimation(fadeOutDuration());
        }
        set.play(buttonAnimator);
        set.start();
    }

    @NonNull
    private Animation fadeOutDuration() {
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(Constants.DURATION_MILLIS_FADE);
        fadeOut.setFillAfter(true);
        return fadeOut;
    }

    void fadeInExpandButton() {
        AnimatorSet set = new AnimatorSet();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, context.getResources().getDisplayMetrics());
        ValueAnimator buttonAnimator = ValueAnimator.ofInt(0, (int) px);

        image_expand.setVisibility(View.VISIBLE);
        buttonAnimator.addUpdateListener(valueAnimator -> {
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = image_expand.getLayoutParams();
            layoutParams.height = value;
            image_expand.setLayoutParams(layoutParams);
        });
        buttonAnimator.setDuration(Constants.EXPAND_DURATION);
        if (duration.getText() != null && !duration.getText().equals("")) {
            duration.startAnimation(fadeInDuration());
        }
        set.play(buttonAnimator);
        buttonAnimator.start();
    }

    private Animation fadeInDuration() {
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(Constants.DURATION_MILLIS_FADE);
        fadeIn.setFillAfter(true);
        return fadeIn;
    }

    void expand(AnimatorListenerAdapter animatorEndListenerAdapter) {
        player.setVisibility(View.VISIBLE);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources().getDisplayMetrics());
        ValueAnimator mAnimator = ValueAnimator.ofInt(0, (int) px);

        mAnimator.addUpdateListener(animatorUpdateListener);
        mAnimator.addListener(animatorEndListenerAdapter);
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.start();
    }

    void collapse(AnimatorListenerAdapter animatorEndListenerAdapter) {
        ValueAnimator mAnimator = ValueAnimator.ofInt(player.getHeight(), 0);

        mAnimator.addUpdateListener(animatorUpdateListener);
        mAnimator.setStartDelay(Constants.START_OFFSET_EXPAND);
        mAnimator.setDuration(Constants.EXPAND_DURATION);
        mAnimator.addListener(animatorEndListenerAdapter);
        mAnimator.start();
    }

    void setProgressBarMax(Podcast podcast) {
        seekBar.setMax((new Utils(context)).getPodcastDuration(podcast.getUri()));
    }
}
