package com.dl.podcastgrossestetes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SimpleAdapter;

public class Utils {

    private static DownloadActivity act;


    public Utils(DownloadActivity activity) {
        act=activity;
    }


    /**
     * Add a value to the list of downloaded podcasts
     * @param seenPodcast string id of the podcast to add to the "seen" list
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
        editor.apply();

        ArrayList<HashMap<String, String>> list = act.getListItem();
        for(HashMap<String,String> item : list){
            if(item.get("description").equals(seenPodcast) && !item.get("day").contains(" ( Téléchargé )")){
                item.put("day", item.get("day")+" ( Téléchargé )");
            }
        }
        updateLayout();
    }

    /**
     * Retrieve the list of downloaded podcasts
     * @return list of downloaded podcasts
     */
    public static List<String> getDownloaded(){
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        String downloaded = sharedPref.getString("Downloaded", "");
        ArrayList<String> seen = new ArrayList<>(Arrays.asList(downloaded.split(",")));
        if(seen.size()>30){
            seen.remove(0);
        }
        return seen;
    }


    /**
     * Parses the html page to get podcast titles and urls
     *
     * @param responseString the whole html page
     *
     */
    public static void parsePage(String responseString, List<String> podcasts, List<String> titles, List<HashMap<String, String>> listItem) {
        String[] lines = responseString.split("\n");
        int i = 0;
        int start, end;
        String currentLine;
        HashMap<String, String> map;
        String title,url;
        while (i < lines.length) {
            currentLine = lines[i];
            if (currentLine.contains("podcast_url")) {
                start = currentLine.indexOf("http");
                end = currentLine.indexOf("\"", start);
                url=currentLine.substring(start, end);
                podcasts.add(url);
                map = new HashMap<>();
                title=getTitleFromUrl(url);
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
    }


    /**
     * Makes a formatted title from the raw url
     * @param url the url
     * @return formatted string
     */
    private static String getTitleFromUrl(String url) {
        String tmp="";
        if(url.contains("du")){
            tmp=url.substring(url.indexOf("du"),url.length()-4);
            tmp=tmp.replace("-"," ");
        }
        if (url.contains("gral")) {
            tmp="L'intégrale "+tmp;
        } else if (url.contains("pite")) {
            tmp="Les pépites "+tmp;
        } else if (url.contains("of")) {
            tmp="Le Best of "+tmp;
        } else if (url.contains("yst")) {
            tmp="L'invité mystère "+tmp;
        } else {
            tmp=(url.substring(url.indexOf("_")+1,url.length()-4)).replace("-"," ");
        }

        // if the date is like "150730"
        try {
            String pattern = "\\d{6}";
            Pattern r = Pattern.compile(pattern);
            Matcher matcher = r.matcher(tmp);
            if (matcher.find()) {
                String nums = matcher.group();
                int year_int = Calendar.getInstance().get(Calendar.YEAR);
                String year = "" + (year_int % 100);
                String year_old = "" + ((year_int % 100) - 1);
                String date = " du ";
                String beginning = nums.substring(0, 2);
                String middle = nums.substring(2, 4);
                String end = nums.substring(4, 6);
                int y, d;
                int m = Integer.parseInt(middle);
                if (beginning.equals(year) || beginning.equals(year_old)) {
                    y = Integer.parseInt("20" + beginning);
                    d = Integer.parseInt(end);

                } else if (end.equals(year) || end.equals(year_old)) {
                    y = Integer.parseInt("20" + end);
                    d = Integer.parseInt(beginning);

                } else {
                    return tmp;
                }
                Calendar calendar = new GregorianCalendar(y, m - 1, d);
                String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRANCE);
                String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRANCE);
                date = date + dayOfWeek + " " + d + " " + month;
                tmp = tmp.substring(matcher.end()+1,tmp.length())+date;
            } else {
                //if date is like "du 10 juillet"
                String noyear = tmp.substring(0,tmp.indexOf("201"));
                pattern = "\\d{2}";
                r = Pattern.compile(pattern);
                matcher = r.matcher(noyear);
                String day;
                int start;
                if (matcher.find()) {
                    start = matcher.start();
                    day = matcher.group();
                } else {

                    pattern = "\\d";
                    r = Pattern.compile(pattern);
                    matcher = r.matcher(noyear);
                    if (matcher.find()) {
                        start = matcher.start();
                        day = matcher.group();
                    } else {
                        return tmp;
                    }
                }
                int m;
                if (tmp.contains("anvier ")) {
                    m = 1;
                } else if (tmp.contains("vrier ")) {
                    m = 2;
                } else if (tmp.contains("ars ")) {
                    m = 3;
                } else if (tmp.contains("vril ")) {
                    m = 4;
                } else if (tmp.contains("mai ")) {
                    m = 5;
                } else if (tmp.contains("uin ")) {
                    m = 6;
                } else if (tmp.contains("uillet ")) {
                    m = 7;
                } else if (tmp.contains("out ")) {
                    m = 8;
                } else if (tmp.contains("eptembre ")) {
                    m = 9;
                } else if (tmp.contains("ctobre ")) {
                    m = 10;
                } else if (tmp.contains("ovembre ")) {
                    m = 11;
                } else if (tmp.contains("cembre ")) {
                    m = 12;
                } else {
                    return tmp;
                }

                if (!getDay(tmp).equals("?")) {
                    return tmp;
                }
                pattern = "20\\d{2}";
                r = Pattern.compile(pattern);
                matcher = r.matcher(tmp);
                if (matcher.find()) {
                    String year = matcher.group();
                    int y = Integer.parseInt(year);
                    int d = Integer.parseInt(day);
                    Calendar calendar = new GregorianCalendar(y, m - 1, d);
                    String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRANCE);
                    String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRANCE);
                    String date = " " + dayOfWeek + " " + d + " " + month;
                    tmp = tmp.substring(0, start) + date;
                }
            }

        } catch (Exception e){
            //do nothing
        }
        return tmp;
    }

    /**
     * Gets the image resource depending on the podcast title
     *
     * @param title the title of the podcast
     * @return the image to display
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
     * @param title the title of the podcast
     * @return the day of the week contained in the title
     */
    private static String getDay(String title) {
        String res = "?";
        title=title.toLowerCase(Locale.FRENCH);
        if (title.contains("lundi")) {
            res = "Lundi";
        } else if (title.contains("mardi") ) {
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
     * Update the layout with values selected by the user
     */
    public static void updateLayout() {
        ArrayList<HashMap<String, String>> list = act.getListItem();
        ArrayList<HashMap<String, String>> newList = new ArrayList<>();
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        boolean integ = sharedPref.getBoolean("integral",true);
        boolean best = sharedPref.getBoolean("best",true);
        boolean mom = sharedPref.getBoolean("moments",true);
        boolean guest = sharedPref.getBoolean("guest",true);
        for(HashMap<String,String> item : list){
            if((item.get("img").equals(String.valueOf(R.drawable.gold)) && mom) ||
                    (item.get("img").equals(String.valueOf(R.drawable.gtlr)) && integ) ||
                    (item.get("img").equals(String.valueOf(R.drawable.best_of)) && best) ||
                    (item.get("img").equals(String.valueOf(R.drawable.anonymous)) && guest)
                    ){
                newList.add(item);
            }
        }
        SimpleAdapter adapt = new SimpleAdapter(
                act.getBaseContext(), newList,
                R.layout.print_item, new String[] { "img", "day",
                "description" }, new int[] { R.id.img, R.id.title,
                R.id.description });
        act.getpodcastList().setAdapter(adapt);
    }

}