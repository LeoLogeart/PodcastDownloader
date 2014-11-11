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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DownloadActivity extends Activity {

	private ArrayList<String> titles;
	private ArrayList<String> podcasts;
	private ProgressDialog progress;
	private ArrayList<HashMap<String, String>> listItem;

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
	 * Parses the html page to get podcast titles and urls
	 * 
	 * @param responseString
	 * @return
	 */
	private ArrayList<String> parsePage(String responseString) {
		podcasts = new ArrayList<String>();
		titles = new ArrayList<String>();
		String[] lines = responseString.split("\n");
		int i = 0;
		int start, end;
		String currentLine;
		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();
		String title;
		while (i < lines.length) {
			currentLine = lines[i];
			if (currentLine.contains("podcast_url")) {
				start = currentLine.indexOf("http");
				end = currentLine.indexOf("\"", start);
				podcasts.add(currentLine.substring(start, end));
			} else if (currentLine.contains("podcast_titre")) {
				start = currentLine.indexOf("value=") + 7;
				end = currentLine.indexOf("\"", start);
				title = currentLine.substring(start, end);
				titles.add(title);

				map = new HashMap<String, String>();
				map.put("day", getDay(title));
				map.put("description", title);
				map.put("img", getImg(title));
				listItem.add(map);
			}
			i++;
		}
		return podcasts;
	}

	/**
	 * Gets the image resource depending on the podcast title
	 * 
	 * @param title
	 * @return
	 */
	private String getImg(String title) {
		String res = String.valueOf(R.drawable.gtlr);
		if (title.contains("intégrale")) {
			res = String.valueOf(R.drawable.gtlr);
		} else if (title.contains("pépite")) {
			res = String.valueOf(R.drawable.gold);
		} else if (title.contains("of")) {
			res = String.valueOf(R.drawable.best_of);
		} else if (title.contains("yst")) {
			res = String.valueOf(R.drawable.anonymous);
		}
		return res;
	}

	/**
	 * Gets the day depending on the podcast title
	 * 
	 * @param title
	 * @return
	 */
	private String getDay(String title) {
		String res = "?";
		if (title.contains("lundi")) {
			res = "Lundi";
		} else if (title.contains("mardi")) {
			res = "Mardi";
		} else if (title.contains("mercredi")) {
			res = "Mercredi";
		} else if (title.contains("jeudi")) {
			res = "Jeudi";
		} else if (title.contains("vendredi")) {
			res = "Vendredi";
		} else if (title.contains("samedi")) {
			res = "Samedi";
		} else if (title.contains("dimanche")) {
			res = "Dimanche";
		}
		return res;
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
			parsePage(result);

			ListView podcastList = (ListView) findViewById(R.id.list_podcast);
			// set the list of titles in listView
			SimpleAdapter adapt = new SimpleAdapter(
					DownloadActivity.this.getBaseContext(), listItem,
					R.layout.print_item, new String[] { "img", "day",
							"description" }, new int[] { R.id.img, R.id.title,
							R.id.description });
			podcastList.setAdapter(adapt);
			// for each title, set the download when clicked (with an alertbox
			// to verify)
			podcastList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> l, View v,
						final int position, long id) {

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