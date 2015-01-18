package com.dl.podcastgrossestetes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class DownloadActivity extends Activity {

	private ArrayList<String> titles;
	private ArrayList<String> podcasts;
	private ProgressDialog progress;
	private ArrayList<HashMap<String, String>> listItem;
	private Utils utils;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private ListView podcastList;
    
    public ListView getpodcastList() {
    	return podcastList;
    }
    
    public ArrayList<HashMap<String, String>> getListItem(){
    	return listItem;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		// Look up the AdView as a resource and load a request.
		AdView adView = new AdView(this);
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId("ca-app-pub-9891261141906247/3743396414");
		LinearLayout adContainer = (LinearLayout)this.findViewById(R.id.adsContainer);

		AdRequest adRequest = new AdRequest.Builder().build();

		adContainer.addView(adView);
		adView.loadAd(adRequest);
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		utils = new Utils(this);
		startDl();
	}

	@Override
	public void onRestart() {
		super.onResume();
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
		if(item.getItemId()==R.id.changelayout){
			createDialog();
		}
		return true;
	}
	
	public void createDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    // Set the dialog title
		boolean[] checked = {sharedPref.getBoolean("integral",true),
				sharedPref.getBoolean("best",true),
				sharedPref.getBoolean("moments",true),
				sharedPref.getBoolean("guest",true)
		};
		editor = sharedPref.edit();
	    builder.setTitle("Afficher")
	    // Specify the list array, the items to be selected by default (null for none),
				// and the listener through which to receive callbacks when
				// items are selected
				.setMultiChoiceItems(
						new String[] { "Intégrales", "Bests of", "Pépites",
								"Invités mystère" }, checked,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
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

							}
						})
	    // Set the action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
						editor.commit();
						Utils.updateLayout();
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {@Override
               public void onClick(DialogInterface dialog, int id) {
                   
               }
           });
	    AlertDialog alertDialog = builder.create();
	    alertDialog.show();
    return;
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
				.execute("http://direct-radio.fr/rtl/podcast/laurent-ruquier/Les-Grosses-Tetes");
	}

	/**
	 * AsyncTask to get the podcast page, parse it and display the list on the
	 * screen
	 */
	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				}
			} catch (ClientProtocolException e) {
				// Do nothing
			} catch (IOException e) {
				// Do nothing
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// To dismiss the dialog
			progress.dismiss();
			if (result == null) {
				connectionProblem();
				return;
			}
			listItem = new ArrayList<HashMap<String, String>>();
			podcasts = new ArrayList<String>();
			titles = new ArrayList<String>();
			Utils.parsePage(result, podcasts, titles, listItem);

			podcastList = (ListView) findViewById(R.id.list_podcast);
			// set the list of titles in listView
			Utils.updateLayout();
			// for each title, set the download when clicked (with an alertbox
			// to verify)
			podcastList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v,
						final int position, long id) {//TODO positon is not well used when special lists are set

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							DownloadActivity.this);
					alertDialogBuilder.setTitle("Télécharger");
					alertDialogBuilder
							.setMessage(
									"Voulez vous télécharger "
											+ titles.get(position))
							.setCancelable(false)
							.setPositiveButton("Oui",
									new DialogInterface.OnClickListener() {
										// start the download manager
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											String url = podcasts.get(position);
											DownloadManager.Request request = new DownloadManager.Request(
													Uri.parse(url));
											request.setDescription("podcast");
											request.setTitle(titles
													.get(position) + ".mp3");
											if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
												request.allowScanningByMediaScanner();
												request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
											}
											request.setDestinationInExternalPublicDir(
													Environment.DIRECTORY_DOWNLOADS,
													titles.get(position)
															+ ".mp3");

											// get download service and enqueue
											// file
											DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
											manager.enqueue(request);
											utils.addSeen(titles.get(position));
										}
									})
							.setNegativeButton("Non",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});

					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();

					// show it
					alertDialog.show();
				}
			});
		}
	}

	/**
	 * Closes the connection.
	 */
	private void connectionProblem() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				DownloadActivity.this);
		alertDialogBuilder.setTitle("Problème de connexion");
		alertDialogBuilder
				.setMessage(
						"Vous n'êtes pas connecté à internet, veuillez réessayer plus tard.")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						DownloadActivity.this.finish();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

}