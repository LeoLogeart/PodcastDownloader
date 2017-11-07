package com.dl.podcastgrossestetes;


import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.HashSet;

import static android.content.Context.DOWNLOAD_SERVICE;

class Utils {

    private Context ctx;
    private File[] downloadedFiles;
    private HashSet<String> filesDownloading;

    Utils(Context activity) {
        ctx = activity;
    }


    boolean isInDlFolder(Podcast podcast) {

        if (downloadedFiles == null) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadedFiles = downloadFolder.listFiles();
        }

        for (File f : downloadedFiles) {
            if (f.getName().contains(podcast.getDescription()) && !isCurrentlyDownloading(podcast.getDescription())) {
                podcast.setUri(f.getAbsolutePath());
                return true;
            }
        }
        return false;
    }

    boolean isCurrentlyDownloading(String title) {
        if (filesDownloading == null) {
            filesDownloading = new HashSet<>();
            DownloadManager mgr = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
            Cursor c = mgr.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED));
            while (c.moveToNext()) {
                filesDownloading.add(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)));
            }
            c.close();
        }
        return filesDownloading.contains(title + ".mp3");
    }

    void saveTime(int currentPosition, String playingUri) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("GrossesTetes", Context.MODE_PRIVATE).edit();
        editor.putInt(playingUri, currentPosition);
        editor.apply();
    }

    int getTime(String podcastUri) {
        return ctx.getSharedPreferences("GrossesTetes", Context.MODE_PRIVATE).getInt(podcastUri, 0);
    }

    int getPodcastDuration(String podcastUri) {
        Uri uri = Uri.parse(podcastUri);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(ctx, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(durationStr);
    }
}
