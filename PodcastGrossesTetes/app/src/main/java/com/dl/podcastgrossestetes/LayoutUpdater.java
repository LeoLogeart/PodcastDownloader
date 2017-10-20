package com.dl.podcastgrossestetes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;


class LayoutUpdater {

    private DownloadActivity act;

    LayoutUpdater(DownloadActivity activity) {
        act = activity;
    }

    /**
     * Update the layout with values selected by the user
     */
    void updateLayout() {
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        boolean integ = sharedPref.getBoolean("integral", true);
        boolean best = sharedPref.getBoolean("best", true);
        boolean mom = sharedPref.getBoolean("moments", true);
        boolean guest = sharedPref.getBoolean("guest", true);
        Utils utils = new Utils(act);
        List<Podcast> shownPodcastList = new ArrayList<>();
        ArrayList<Podcast> podcastList = act.getPodcastListView();
        for (Podcast pod : podcastList) {
            if ((mom && pod.getType().equals(Podcast.Type.PEPITE)) ||
                    (best && pod.getType().equals(Podcast.Type.BEST_OF)) ||
                    (integ && pod.getType().equals(Podcast.Type.INTEGRALE)) ||
                    (guest && pod.getType().equals(Podcast.Type.INVITE_MYSTERE))) {
                if (utils.isInDlFolder(pod)) {
                    pod.setDownloaded();
                } else if (utils.isCurrentlyDownloading(pod.getDescription())) {
                    pod.setDownloading();
                }
                shownPodcastList.add(pod);
            }
        }
        PodcastAdapter adapt = new PodcastAdapter(shownPodcastList, act);
        act.getpodcastList().setAdapter(adapt);
    }


    /**
     * Add a value to the list of downloaded podcasts
     *
     * @param downloadingPodcast string id of the podcast to add to the "seen" list
     */
    void addDownloadingIcon(Podcast downloadingPodcast) {
        downloadingPodcast.setDownloading();
        act.getpodcastList().getAdapter().notifyDataSetChanged();

    }

    /**
     * Closes the connection.
     */
    void connectionProblem() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle("Problème de connexion");
        alertDialogBuilder
                .setMessage(
                        "Impossible de contacter le serveur, veuillez réessayer plus tard.")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> act.finish());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showDownloadErrorDialog(DownloadActivity downloadActivity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                downloadActivity);
        alertDialogBuilder.setTitle("Problème de stockage");
        alertDialogBuilder
                .setMessage(
                        "Impossible d'écrire sur la mémoire externe.")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> downloadActivity.finish());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void createDownloadConfirmationDialog(final Podcast podcast) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle("Télécharger");
        alertDialogBuilder
                .setMessage(
                        "Voulez vous télécharger "
                                + podcast.getDescription())
                .setCancelable(false)
                .setPositiveButton("Oui",
                        (dialog, id) -> act.DownloadPodcast(podcast))
                .setNegativeButton("Non",
                        (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Create the dialog that will ask which display the user wants
     */
    void createMenuDialog() {
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        // Set the dialog title
        boolean[] checked = {sharedPref.getBoolean("integral", true),
                sharedPref.getBoolean("best", true),
                sharedPref.getBoolean("moments", true),
                sharedPref.getBoolean("guest", true)
        };
        SharedPreferences.Editor editor = sharedPref.edit();
        builder.setTitle("Afficher");
        builder.setMultiChoiceItems(
                new String[]{"Intégrales", "Bests of", "Pépites",
                        "Invités mystère"}, checked,
                (dialog, which, isChecked) -> {
                    switch (which) {
                        case 0:
                            editor.putBoolean("integral", isChecked);
                            break;
                        case 1:
                            editor.putBoolean("best", isChecked);
                            break;
                        case 2:
                            editor.putBoolean("moments", isChecked);
                            break;
                        case 3:
                            editor.putBoolean("guest", isChecked);
                            break;
                    }

                });
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            editor.apply();
            updateLayout();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}