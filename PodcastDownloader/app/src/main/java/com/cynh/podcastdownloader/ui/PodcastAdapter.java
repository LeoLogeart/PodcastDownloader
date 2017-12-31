package com.cynh.podcastdownloader.ui;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cynh.podcastdownloader.R;
import com.cynh.podcastdownloader.context.DownloadActivity;
import com.cynh.podcastdownloader.model.Podcast;
import com.cynh.podcastdownloader.utils.Constants;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastViewHolder> {
    private final List<Podcast> podcasts;
    private boolean animating = false;
    private DownloadActivity context;

    public PodcastAdapter(List<Podcast> items, DownloadActivity ctx) {
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
        Podcast currentPodcast = podcasts.get(position);
        holder.initHolder(currentPodcast);

        if (currentPodcast.getStatus().equals(Podcast.Status.DOWNLOADED)) {
            holder.InitDownloadedHolder(currentPodcast);
        }
        holder.getCardView().setOnClickListener(view -> onCardViewClick(holder, position));
    }

    private void onCardViewClick(PodcastViewHolder holder, int position) {
        Podcast podcast = podcasts.get(position);
        if (podcast.getStatus().equals(Podcast.Status.DOWNLOADED)) {
            if (animating) {
                return;
            }
            logFirebase(podcast);
            holder.OnDownloadedCardViewClick(podcast);
            final Handler handler = new Handler();
            handler.postDelayed(() -> animating = false, Constants.ANIMATION_DURATION);

        } else {
            (new LayoutUpdater(context)).createDownloadConfirmationDialog(podcast);
        }
    }

    private void logFirebase(Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, podcast.getUri());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, podcast.getSubtitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "podcast");
        FirebaseAnalytics firebase = context.getFirebase();
        if(firebase!=null)
            firebase.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        animating = true;
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

}

