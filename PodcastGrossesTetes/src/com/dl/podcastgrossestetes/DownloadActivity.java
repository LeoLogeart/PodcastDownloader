package com.dl.podcastgrossestetes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DownloadActivity extends Activity {

	private ArrayList<String> titles;
	private ArrayList<String> podcasts;
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Begin", "onCreate");
		setContentView(R.layout.activity_download);
		progress = new ProgressDialog(this);
		progress.setTitle("Récupération des podcasts");
		progress.setMessage("Patientez pendant la vérification des podcasts disponibles...");
		progress.show();

		new RequestTask()
				.execute("http://direct-radio.fr/rtl/podcast/laurent-ruquier/Les-Grosses-Tetes");
	}

	@Override
	public void onRestart() {
		super.onResume();
		Log.d("Begin", "onResume");
		titles = new ArrayList<String>();
		podcasts = new ArrayList<String>();
		new RequestTask()
				.execute("http://direct-radio.fr/rtl/podcast/laurent-ruquier/Les-Grosses-Tetes");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.download, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Parses the html page to get podcast titles and urls
	 * 
	 * @param responseString
	 * @return
	 */
	private ArrayList<String> parsePage(String responseString) {
		Log.d("Begin", "Parse");
		podcasts = new ArrayList<String>();
		titles = new ArrayList<String>();
		String[] lines = responseString.split("\n");
		int i = 0;
		int start, end;
		String currentLine;
		while (i < lines.length) {
			currentLine = lines[i];
			if (currentLine.contains("podcast_url")) {
				start = currentLine.indexOf("http");
				end = currentLine.indexOf("\"", start);
				podcasts.add(currentLine.substring(start, end));
				// Log.d("url",currentLine.substring(start, end));
			} else if (currentLine.contains("podcast_titre")) {
				start = currentLine.indexOf("value=") + 7;
				end = currentLine.indexOf("\"", start);
				titles.add(currentLine.substring(start, end));
				// Log.d("title",currentLine.substring(start, end));
			}
			i++;
		}
		return podcasts;
	}

	/**
	 * AsyncTask to get the podcast page, parse it and display the list on the
	 * screen
	 * 
	 * @author leo
	 * 
	 */
	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			Log.d("begin", "doInBackground");
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
				} else {
					// connectionProblem();
				}
			} catch (ClientProtocolException e) {
				// connectionProblem();
			} catch (IOException e) {
				// connectionProblem();
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d("onPostExecute", "begin");
			// To dismiss the dialog
			progress.dismiss();
			if (result == null) {
				Log.d("onPostExecute", "null");
				connectionProblem();
				return;
			}
			parsePage(result);

			ListView mContactList = (ListView) findViewById(R.id.list_podcast);
			// set the list of titles in listView
			ArrayAdapter<String> adapt = new ArrayAdapter<String>(
					getApplicationContext(), R.layout.list_item, titles);

			mContactList.setAdapter(adapt);
			// for each title, set the download when clicked (with an alertbox
			// to verify)
			mContactList.setOnItemClickListener(new OnItemClickListener() {

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
										public void onClick(
												DialogInterface dialog, int id) {
											Log.d("download",
													titles.get(position)
															+ " ------ "
															+ podcasts
																	.get(position));
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
													Environment.DIRECTORY_DOWNLOADS,// TODO
																					// PODCASTS?
													titles.get(position)
															+ ".mp3");

											// get download service and enqueue
											// file
											DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
											Log.d("download",
													"Starting to download");
											manager.enqueue(request);

										}
									})
							.setNegativeButton("Non",
									new DialogInterface.OnClickListener() {
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

	private void connectionProblem() {
		Log.d("Begin", "connectionProblem");
		// Closes the connection.
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				DownloadActivity.this);
		alertDialogBuilder.setTitle("Problème de connexion");
		alertDialogBuilder
				.setMessage(
						"Vous n'êtes pas connecté à internet, veuillez réessayer plus tard.")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 DownloadActivity.this.finish();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

}