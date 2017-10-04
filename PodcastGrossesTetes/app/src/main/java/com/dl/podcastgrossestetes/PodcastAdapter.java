package com.dl.podcastgrossestetes;


import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {
    private final List<Podcast> podcasts;
    DownloadActivity context;

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
        holder.image.setImageDrawable(ContextCompat.getDrawable(context, podcasts.get(position).getImage()));
        holder.title.setText(podcasts.get(position).getDay());
        holder.description.setText(podcasts.get(position).getDescription());

        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADING)) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }

        if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
            holder.image_right.setVisibility(View.VISIBLE);
        } else {
            holder.image_right.setVisibility(View.GONE);
        }

        holder.cv.setOnClickListener(v -> {
            // item clicked
            if (podcasts.get(position).getStatus().equals(Podcast.Status.DOWNLOADED)) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(podcasts.get(position).getUri());
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                context.startActivity(intent);
            } else {
                (new LayoutUpdater(context)).createDownloadConfirmationDialog(podcasts.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public static class PodcastViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView title;
        TextView description;
        ImageView image;
        ImageView image_right;
        ProgressBar progressBar;

        PodcastViewHolder(View podcastView) {
            super(podcastView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            image = (ImageView) podcastView.findViewById(R.id.img);
            image_right = (ImageView) podcastView.findViewById(R.id.img_play);
            title = (TextView) podcastView.findViewById(R.id.title);
            description = (TextView) podcastView.findViewById(R.id.description);
            progressBar = (ProgressBar) podcastView.findViewById(R.id.progressBar);
        }
    }
}