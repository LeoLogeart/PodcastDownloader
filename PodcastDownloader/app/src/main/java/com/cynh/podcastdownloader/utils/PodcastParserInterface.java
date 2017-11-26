package com.cynh.podcastdownloader.utils;

import com.cynh.podcastdownloader.model.Podcast;

import java.util.ArrayList;

public interface PodcastParserInterface {
    ArrayList<Podcast> parsePage(String RssString);
}
