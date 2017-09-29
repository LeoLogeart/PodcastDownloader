package com.dl.podcastgrossestetes;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class PodcastAdapter extends ArrayAdapter<Podcast> {

    int resource;
    Context context;

    public PodcastAdapter(Context context, int resource, List<Podcast> items) {
        super(context, resource, items);
        this.resource = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout podcastView;
        Podcast podcast = getItem(position);
        if (convertView == null) {
            podcastView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater) getContext().getSystemService(inflater);
            vi.inflate(resource, podcastView, true);
        } else {
            podcastView = (LinearLayout) convertView;
        }
        ImageView image = (ImageView) podcastView.findViewById(R.id.img);
        image.setImageDrawable(ContextCompat.getDrawable(context, podcast.getImage()));
        TextView title = (TextView) podcastView.findViewById(R.id.title);
        title.setText(podcast.getDay());
        TextView description = (TextView) podcastView.findViewById(R.id.description);
        description.setText(podcast.getDescription());
        ProgressBar progressBar = (ProgressBar) podcastView.findViewById(R.id.progressBar);

        if (podcast.getStatus().equals(Podcast.Status.DOWNLOADING)) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        ImageView playImg = (ImageView) podcastView.findViewById(R.id.img_play);
        if (podcast.getStatus().equals(Podcast.Status.DOWNLOADED)) {
            playImg.setVisibility(View.VISIBLE);
        } else {
            playImg.setVisibility(View.GONE);
        }

        return podcastView;
    }
}