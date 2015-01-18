package com.dl.podcastgrossestetes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
	
	private static Activity act;
	
	
	public Utils(Activity activity) {
		act=activity;
	}

	public static Activity getActivity(){
		return act;
	}
	
	/**
	 * Add a vlue to the list of downloaded podcasts
	 * @param seenPodcast
	 */
	public void addSeen(String seenPodcast){
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		String downloaded = sharedPref.getString("Downloaded",null);
		SharedPreferences.Editor editor = sharedPref.edit();
		if(downloaded==null){
			editor.putString("Downloaded",seenPodcast);
		} else {
			editor.putString("Downloaded",downloaded+","+seenPodcast);
		}
		editor.commit();
	}
	
	/**
	 * Retrieve the list of downloaded podcasts
	 * @return
	 */
	public static List<String> getDownloaded(){
		SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
		String downloaded = sharedPref.getString("Downloaded", "");
		ArrayList<String> seen = new ArrayList<String>(Arrays.asList(downloaded.split(",")));
		if(seen.size()>30){
			seen.remove(0);
		}
		return seen;
	}

	
	/**
	 * Parses the html page to get podcast titles and urls
	 * 
	 * @param responseString
	 * @return
	 */
	public static void parsePage(String responseString, List<String> podcasts, List<String> titles, List<HashMap<String, String>> listItem) {
		String[] lines = responseString.split("\n");
		int i = 0;
		int start, end;
		String currentLine;
		HashMap<String, String> map = new HashMap<String, String>();
		String title;
		while (i < lines.length) {
			currentLine = lines[i];
			if (currentLine.contains("podcast_url")) {
				start = currentLine.indexOf("http");
				end = currentLine.indexOf("\"", start);
				podcasts.add(currentLine.substring(start, end));
			} else if (currentLine.contains("podcast_titre")) {
				map = new HashMap<String, String>();
				start = currentLine.indexOf("value=") + 7;
				end = currentLine.indexOf("\"", start);
				title = currentLine.substring(start, end);
				titles.add(title);
				if(Utils.getDownloaded().contains(title)){
					map.put("day", getDay(title)+" ( Téléchargé )");
				} else {
					map.put("day", getDay(title));
				}

				map.put("description", title);
				map.put("img", getImg(title));
				listItem.add(map);
			}
			i++;
		}
		return;
	}
	


	/**
	 * Gets the image resource depending on the podcast title
	 * 
	 * @param title
	 * @return
	 */
	private static String getImg(String title) {
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
	private static String getDay(String title) {
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

}
