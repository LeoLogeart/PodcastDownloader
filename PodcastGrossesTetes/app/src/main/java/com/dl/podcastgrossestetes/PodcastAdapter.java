package com.dl.podcastgrossestetes;


import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

class PodcastAdapter extends RecyclerView.Adapter<PodcastViewHolder> {
    private final List<Podcast> podcasts;
    private boolean animating = false;
    private DownloadActivity context;

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
            holder.InitDownloadedHolder(podcasts.get(position));
        } else {
            holder.setExpandButtonGone();
        }

        holder.getCardView().setOnClickListener(v -> onCardViewClick(holder, position));
    }

    private void onCardViewClick(PodcastViewHolder holder, int position) {
        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
            if (animating) {
                return;
            }
            animating = true;
            holder.OnDownloadedCardViewClick(podcasts.get(position));
            final Handler handler = new Handler();
            handler.postDelayed(() -> animating = false, Constants.ANIMATION_DURATION);

        } else {
            (new LayoutUpdater(context)).createDownloadConfirmationDialog(podcasts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

}

