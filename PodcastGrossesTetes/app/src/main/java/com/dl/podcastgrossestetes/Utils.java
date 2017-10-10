package com.dl.podcastgrossestetes;


import android.app.DownloadManager;
import android.database.Cursor;
import android.os.Environment;

import java.io.File;
import java.util.HashSet;

import static android.content.Context.DOWNLOAD_SERVICE;

class Utils {

    private DownloadActivity act;
    private File[] downloadedFiles;
    private HashSet<String> filesDownloading;

    Utils(DownloadActivity activity) {
        act = activity;
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
            DownloadManager mgr = (DownloadManager) act.getSystemService(DOWNLOAD_SERVICE);
            Cursor c = mgr.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED));
            while (c.moveToNext()) {
                filesDownloading.add(c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)));
            }
            c.close();
        }
        return filesDownloading.contains(title + ".mp3");
    }
}
