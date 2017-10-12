package com.dl.podcastgrossestetes;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class DownloadActivity extends Activity implements Observer {

    private ProgressDialog progress;
    private ArrayList<Podcast> podcastsList;
    private PodcastParser parser;
    private LayoutUpdater layoutUpdater;
    private RecyclerView podcastListView;
    private PodcastViewHolder currentPlayerHolder;

    BroadcastReceiver onDlComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            isDlSuccessful(dwnId);
        }
    };
    private MediaPlayer mediaPlayer;
    private Uri playingUri;

    private void isDlSuccessful(Long dwnId) {
        DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor c = mgr.query(new DownloadManager.Query().setFilterById(dwnId));

        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                layoutUpdater.updateLayout();
            }
        }
    }

    public RecyclerView getpodcastList() {
        return podcastListView;
    }

    public ArrayList<Podcast> getPodcastListView() {
        return podcastsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        MediaPlayerObservableObject.getInstance().addObserver(this);
        // Look up the AdView as a resource and load a request.
        setupAd();
        initFields();
        startDl();
    }

    private void initFields() {
        parser = new PodcastParser();
        layoutUpdater = new LayoutUpdater(this);

        podcastListView = (RecyclerView) findViewById(R.id.list_podcast);
        LinearLayoutManager llm = new LinearLayoutManager(DownloadActivity.this);
        podcastListView.setLayoutManager(llm);
        registerReceiver(onDlComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void setupAd() {
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-9891261141906247/3743396414");
        LinearLayout adContainer = (LinearLayout) this.findViewById(R.id.adsContainer);

        AdRequest adRequest = new AdRequest.Builder().build();

        adContainer.addView(adView);
        adView.loadAd(adRequest);
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer!=null)
        {
            (new Utils(this)).saveTime(mediaPlayer.getCurrentPosition(),getPlayingUri());
        }
        super.onDestroy();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        // To dismiss the loading dialog
        progress.dismiss();
        startDl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.changelayout) {
            layoutUpdater.createMenuDialog();
        }
        return true;
    }


    /**
     * Launches the progress dialog and the downloader.
     */
    private void startDl() {
        progress = new ProgressDialog(this);
        progress.setTitle("Récupération des podcasts");
        progress.setMessage("Patientez pendant la vérification des podcasts disponibles...");
        progress.show();
        new RequestTask()
                .execute("http://www.rtl.fr/podcast/les-grosses-tetes.xml");
    }

    void DownloadPodcast(Podcast podcast) {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(podcast.getUrl()));
        request.setDescription("podcast");
        request.setTitle(podcast.getDescription() + ".mp3");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        try {
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    podcast.getDescription() + ".mp3");
        } catch (IllegalStateException e) {
            layoutUpdater.showDownloadErrorDialog(this);
            return;
        }

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        layoutUpdater.addDownloadingIcon(podcast);
    }

    public MediaPlayer getMediaPlayer(Uri uri) {
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        playingUri = uri;
        mediaPlayer = MediaPlayer.create(this, uri);
        return mediaPlayer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Uri getPlayingUri() {
        return playingUri;
    }

    public PodcastViewHolder getCurrentPlayerHolder() {
        return currentPlayerHolder;
    }

    public void setCurrentPlayerHolder(PodcastViewHolder currentPlayerHolder) {
        this.currentPlayerHolder = currentPlayerHolder;
    }

    @Override
    public void update(Observable observable, Object o) {
        if(mediaPlayer.isPlaying())
        {
            currentPlayerHolder.onPlayPauseClick();
        }
    }


    /**
     * AsyncTask to get the podcast page, parse it and display the list on the
     * screen
     */
    private class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpURLConnection urlConnection = null;
            String responseString = null;
            try {
                URL url = new URL(uri[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // read it with BufferedReader
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                responseString = sb.toString();
                br.close();
            } catch (Exception e) {
                //do nothing
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // To dismiss the dialog
            progress.dismiss();
            if (result == null) {
                layoutUpdater.connectionProblem();
                return;
            }

            podcastsList = new ArrayList<>();

            parser.parsePage(result, podcastsList);
            // set the list of titles in listView
            layoutUpdater.updateLayout();
            // for each title, set the download when clicked (with an alertbox
            // to verify)
            /*llm.OnItemTouchListener((l, v, position, id) -> {
               podca
            });*/
        }
    }

}