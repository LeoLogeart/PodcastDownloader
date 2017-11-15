package com.dl.podcastgrossestetes.utils;

import com.dl.podcastgrossestetes.model.Podcast;

import java.util.ArrayList;

public interface PodcastParserInterface {
    ArrayList<Podcast> parsePage(String RssString);
}
