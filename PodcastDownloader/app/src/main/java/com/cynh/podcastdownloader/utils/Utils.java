package com.cynh.podcastdownloader.utils;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import com.cynh.podcastdownloader.R;
import com.cynh.podcastdownloader.context.DownloadActivity;
import com.cynh.podcastdownloader.model.Podcast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import static android.content.Context.DOWNLOAD_SERVICE;

public class Utils {

    private Context ctx;
    private File[] downloadedFiles;
    private HashSet<String> filesDownloading;

    public Utils(Context activity) {
        ctx = activity;
    }


    public boolean isInDlFolder(Podcast podcast) {

        if (downloadedFiles == null) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadedFiles = downloadFolder.listFiles();
            if (downloadedFiles == null) return false;
        }
        String podcastName;
        for (File f : downloadedFiles) {
            podcastName = podcast.getSubtitle().replaceAll("\\?","");
            if (f.getName().contains(podcastName) && !isCurrentlyDownloading(podcastName)) {
                podcast.setUri(f.getAbsolutePath());
                return true;
            }
        }
        return false;
    }

    public boolean isCurrentlyDownloading(String title) {
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

    public void saveTime(int currentPosition, String playingUri) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE).edit();
        editor.putInt(playingUri, currentPosition);
        editor.apply();
    }

    public int getTime(String podcastUri) {
        return ctx.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE).getInt(podcastUri, 0);
    }

    public int getPodcastDuration(String podcastUri) {
        int res = 0;
        try {
            Uri uri = Uri.parse(podcastUri);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(ctx, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            res = Integer.parseInt(durationStr);
        } catch (Exception e) {
            askToRemovePodcast(podcastUri);
        }
        return res;
    }

    private void askToRemovePodcast(String podcastUri) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
            alertDialogBuilder.setTitle(R.string.corrupted_file_title);
            alertDialogBuilder
                    .setMessage(R.string.corrupted_file_description)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, id) -> deleteFile(podcastUri))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
    }

    private void deleteFile(String podcastUri) {
        File podcast = new File(podcastUri);
        podcast.delete();
        ((DownloadActivity)ctx).updateLayout();
    }

    public void savePodcastList(ArrayList<Podcast> podcastsList) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE).edit();
        StringBuilder listBuilder = new StringBuilder();
        for (Podcast podcast : podcastsList) {
            listBuilder.append(podcast.getTitle());
            listBuilder.append("$$").append(podcast.getSubtitle());
            listBuilder.append("$$").append(podcast.getImage());
            listBuilder.append("$$").append(podcast.getUrl());
            listBuilder.append("$$").append(podcast.getType());
            listBuilder.append("$$").append(podcast.getDuration());
            listBuilder.append(";");
        }
        editor.putString(Constants.PODCASTS, listBuilder.toString());
        editor.apply();
    }

    public ArrayList<Podcast> retrievePodcastList() {
        ArrayList<Podcast> podcastsList = new ArrayList<>();
        String list = ctx.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE).getString(Constants.PODCASTS, "");
        String[] podcastStrings = list.split(";");
        for (String podcast : podcastStrings) {
            String[] podcastParts = podcast.split("\\$\\$");
            if (podcastParts.length == 6) {
                podcastsList.add(new Podcast(podcastParts[0], podcastParts[1], Integer.parseInt(podcastParts[2]), podcastParts[3], podcastParts[4], Integer.parseInt(podcastParts[5])));
            }
        }
        return podcastsList;
    }
}
