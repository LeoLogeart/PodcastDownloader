package com.dl.podcastgrossestetes.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
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
        int resId = R.anim.animation_from_bottom;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(act, resId);
        ((RecyclerView)act.findViewById(R.id.list_podcast)).setLayoutAnimation(animation);
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

    public void showDownloadErrorDialog(DownloadActivity downloadActivity) {
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
                        (dialog, id) -> act.grantStoragePermission(podcast))
                .setNegativeButton("Non",
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

    public void createReadPhoneStateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setTitle("Autoriser la gestion d'appels.");
        alertDialogBuilder
                .setMessage(
                        "La gestion d'appels téléphoniques permet à l'application d'arrêter la lecture du podcast lors d'un appel.")
                .setCancelable(false)
                .setPositiveButton("OK",
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
        alertDialogBuilder.setTitle("Impossible de télécharger!");
        alertDialogBuilder
                .setMessage(
                        "L'application ne peut pas télécharger le podcast sans l'accès à la mémoire.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
