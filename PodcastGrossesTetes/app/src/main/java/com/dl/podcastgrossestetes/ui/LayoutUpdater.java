package com.dl.podcastgrossestetes.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.dl.podcastgrossestetes.R;
import com.dl.podcastgrossestetes.context.DownloadActivity;
import com.dl.podcastgrossestetes.model.Podcast;
import com.dl.podcastgrossestetes.utils.Constants;
import com.dl.podcastgrossestetes.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class LayoutUpdater {

    private DownloadActivity act;

    public LayoutUpdater(DownloadActivity activity) {
        act = activity;
    }

    /**
     * Update the layout with values selected by the user
     */
    public void updateLayout() {
        List<Podcast> shownPodcastList = new ArrayList<>();
        ArrayList<Podcast> podcastList = act.getPodcastListView();
        Utils utils = new Utils(act);
        for (Podcast pod : podcastList) {
            addSelectedPodcast(shownPodcastList, pod, utils);
        }
        PodcastAdapter adapt = new PodcastAdapter(shownPodcastList, act);
        act.getpodcastList().setAdapter(adapt);
        int resId = R.anim.animation_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(act, resId);
        ((RecyclerView) act.findViewById(R.id.list_podcast)).setLayoutAnimation(animation);
    }

    private void addSelectedPodcast(List<Podcast> shownPodcastList, Podcast pod, Utils utils) {
        if (Podcast.getPodcastTypes() == null) {
            Log.e("PGT", "You didn't set the podcast types : Podcast.setTypes(...)");
            return;
        }
        for (String type : Podcast.getPodcastTypes()) {
            if (PodcastTypeAccepted(pod, type)) {
                if (utils.isInDlFolder(pod)) {
                    pod.setDownloaded();
                } else if (utils.isCurrentlyDownloading(pod.getSubtitle())) {
                    pod.setDownloading();
                }
                shownPodcastList.add(pod);
                return;
            }
        }
    }

    private boolean PodcastTypeAccepted(Podcast pod, String type) {
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        return pod.getType().equals(type) && sharedPref.getBoolean(type, true);
    }


    /**
     * Add a value to the list of downloaded podcasts
     *
     * @param downloadingPodcast string id of the podcast to add to the "seen" list
     */
    public void addDownloadingIcon(Podcast downloadingPodcast) {
        downloadingPodcast.setDownloading();
        act.getpodcastList().getAdapter().notifyDataSetChanged();

    }

    /**
     * Closes the connection.
     */
    public void connectionProblem() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle(R.string.connection_issue_title);
        alertDialogBuilder
                .setMessage(R.string.connection_issue_description)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, id) -> act.finish());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showDownloadErrorDialog(DownloadActivity downloadActivity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                downloadActivity);
        alertDialogBuilder.setTitle(R.string.storage_issue_title);
        alertDialogBuilder
                .setMessage(R.string.storage_issue_description)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, id) -> downloadActivity.finish());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void createDownloadConfirmationDialog(final Podcast podcast) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle(R.string.download_title);
        alertDialogBuilder
                .setMessage(
                        act.getString(R.string.download_description)
                                + podcast.getSubtitle())
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        (dialog, id) -> act.grantStoragePermission(podcast))
                .setNegativeButton(R.string.no,
                        (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Create the dialog that will ask which display the user wants
     */
    public void createMenuDialog() {
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        boolean[] checked = new boolean[Podcast.getPodcastTypes().length];
        int i = 0;
        for (String type : Podcast.getPodcastTypes()) {
            checked[i] = sharedPref.getBoolean(type, true);
            i++;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        builder.setTitle(R.string.print);
        builder.setMultiChoiceItems(Podcast.getPodcastTypes(), checked,
                (dialog, which, isChecked) -> editor.putBoolean(Podcast.getPodcastTypes()[which], isChecked));
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            editor.apply();
            updateLayout();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void createReadPhoneStateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle(R.string.allow_phone_title);
        alertDialogBuilder
                .setMessage(R.string.allow_phone_description)
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        (dialog, id) -> {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(act,
                                    new String[]{android.Manifest.permission.READ_PHONE_STATE}, Constants.READ_PHONE_STATE_CODE);
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void createStoragePermissionDeniedDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle(R.string.impossible_download_memory_title);
        alertDialogBuilder
                .setMessage(R.string.impossible_download_memory_description)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
