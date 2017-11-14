package com.dl.podcastgrossestetes.context;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.dl.podcastgrossestetes.R;
import com.dl.podcastgrossestetes.model.Podcast;
import com.dl.podcastgrossestetes.ui.LayoutUpdater;
import com.dl.podcastgrossestetes.ui.PodcastAdapter;
import com.dl.podcastgrossestetes.ui.PodcastViewHolder;
import com.dl.podcastgrossestetes.utils.Constants;
import com.dl.podcastgrossestetes.utils.MediaBrowserManager;
import com.dl.podcastgrossestetes.utils.PodcastParser;
import com.dl.podcastgrossestetes.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DownloadActivity extends Activity {

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 100;
    boolean serviceBound = false;
    private ProgressDialog progress;
    private ArrayList<Podcast> podcastsList;
    private PodcastParser parser;
    private LayoutUpdater layoutUpdater;
    private RecyclerView podcastListView;
    BroadcastReceiver onDlComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            isDlSuccessful(dwnId);
        }
    };
    private PodcastViewHolder currentPlayerHolder;
    private MediaBrowserManager mediaBrowsermanager;
    private Podcast playingPodcast;
    private Podcast waitingPodcast;
    private FirebaseAnalytics firebase;

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

    @Override
    public void onResume() {
        super.onResume();
    }

    public void grantStoragePermission(Podcast podcast) {
        if (ContextCompat.checkSelfPermission(DownloadActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DownloadActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_CODE);
            waitingPodcast = podcast;
        } else
        {
            downloadPodcast(podcast);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(waitingPodcast!=null){
                        downloadPodcast(waitingPodcast);
                        waitingPodcast = null;
                    }
                } else {
                    layoutUpdater.createStoragePermissionDeniedDialog();
                }
                return;
            }

        }
    }

    private void grantReadPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(DownloadActivity.this,
                android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DownloadActivity.this,
                    android.Manifest.permission.READ_PHONE_STATE)) {
                layoutUpdater.createReadPhoneStateDialog();

            } else {
                ActivityCompat.requestPermissions(DownloadActivity.this,
                        new String[]{android.Manifest.permission.READ_PHONE_STATE}, Constants.READ_PHONE_STATE_CODE);
            }
        }
    }

    public MediaBrowserManager connect() {
        mediaBrowsermanager = new MediaBrowserManager(this, playingPodcast);
        mediaBrowsermanager.connect();
        return mediaBrowsermanager;
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
        setupAd();
        initFields();
        startDl();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            grantReadPhoneStatePermission();
        }
        firebase = FirebaseAnalytics.getInstance(this);
        PodcastAdapter adapt = new PodcastAdapter(new ArrayList<Podcast>(), this);
        getpodcastList().setAdapter(adapt);
    }

    private void initFields() {
        parser = new PodcastParser();
        layoutUpdater = new LayoutUpdater(this);

        podcastListView = findViewById(R.id.list_podcast);
        LinearLayoutManager llm = new LinearLayoutManager(DownloadActivity.this);
        podcastListView.setLayoutManager(llm);
        registerReceiver(onDlComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void setupAd() {
        MobileAds.initialize(this,"ca-app-pub-9891261141906247/3743396414");
        AdView adContainer = this.findViewById(R.id.adsContainer);

        AdRequest adRequest = new AdRequest.Builder().build();

        adContainer.loadAd(adRequest);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(onDlComplete);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
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

    public void downloadPodcast(Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, podcast.getUri());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, podcast.getDescription());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "podcast");
        firebase.logEvent("podcast_download", bundle);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(podcast.getUrl()));
        request.setDescription("podcast");
        request.setTitle(podcast.getDescription() + ".mp3");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

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


    public Uri getPlayingUri() {
        if (playingPodcast == null) {
            return Uri.parse("");
        }
        return Uri.parse(playingPodcast.getUri());
    }

    public PodcastViewHolder getCurrentPlayerHolder() {
        return currentPlayerHolder;
    }

    public void setCurrentPlayerHolder(PodcastViewHolder currentPlayerHolder) {
        this.currentPlayerHolder = currentPlayerHolder;
    }

    public void setPodcast(Podcast podcast) {
        playingPodcast = podcast;
    }

    public Podcast getPlayingPodcast() {
        return playingPodcast;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                mediaBrowsermanager.dispatchMediaButtonEvent(event);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public FirebaseAnalytics getFirebase() {
        return firebase;
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
            progress.dismiss();
            if (result == null) {
                podcastsList = (new Utils(DownloadActivity.this)).retrievePodcastList();
                if (podcastsList.isEmpty()) {
                    layoutUpdater.connectionProblem();
                }
                return;
            } else {
                podcastsList = new ArrayList<>();
            }

            parser.parsePage(result, podcastsList);
            (new Utils(DownloadActivity.this)).savePodcastList(podcastsList);
            layoutUpdater.updateLayout();
        }
    }

}