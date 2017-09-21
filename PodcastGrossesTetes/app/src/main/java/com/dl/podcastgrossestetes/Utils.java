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
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;

import static com.dl.podcastgrossestetes.R.drawable.anonymous;
import static com.dl.podcastgrossestetes.R.drawable.best_of;
import static com.dl.podcastgrossestetes.R.drawable.gold;
import static com.dl.podcastgrossestetes.R.drawable.gtlr;

public class Utils {

    private DownloadActivity act;
    private String formattedDate;


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
            editor.putString("Downloaded", downloaded + "," + seenPodcast);
        }
        editor.apply();

        ArrayList<HashMap<String, String>> list = act.getListItem();
        for(HashMap<String,String> item : list){
            if(item.get("description").equals(seenPodcast) && !item.get("day").contains(" ( Téléchargé )")){
                item.put("day", item.get("day")+" ( Téléchargé )");
            }
        }
        ((BaseAdapter) act.getpodcastList().getAdapter()).notifyDataSetChanged();
    }

    /**
     * Retrieve the list of downloaded podcasts
     * @return list of downloaded podcasts
     */
    public List<String> getDownloaded(){
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
    public void parsePage(String responseString, List<String> podcasts, List<String> titles, List<HashMap<String, String>> listItem) {
        String[] lines = responseString.split("\n");
        int i = 0;
        int start, end;
        String currentLine;
        HashMap<String, String> map;
        String title,url;
        while (i < lines.length) {
            currentLine = lines[i];
            if (currentLine.contains("enclosure url")) {
                start = currentLine.indexOf("http");
                end = currentLine.indexOf("\"", start);
                url=currentLine.substring(start, end);
                podcasts.add(url);
                map = new HashMap<>();
                title=getTitleFromUrl(url);
                titles.add(title);
                if(getDownloaded().contains(title)){
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
    private String getTitleFromUrl(String url) {
        String tmp;
        StringBuilder sb = new StringBuilder();
        appendPodcastType(url, sb);
        tmp=(url.substring(url.indexOf("_")+1,url.length()-4)).replace("-"," ");
        try {
            if(!matchDate1(tmp) && !matchDate2(tmp) && !matchDate3(tmp))
            {
                //if date is like "du 10 juillet"
                String noyear = tmp;
                if(tmp.contains("201")) {
                    noyear = tmp.substring(0, tmp.indexOf("201"));
                }
                String pattern = "\\d{2}";
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(noyear);
                String day;
                if (matcher.find()) {
                    day = matcher.group();
                } else {
                    // du 2 aout
                    pattern = "\\d";
                    r = Pattern.compile(pattern);
                    matcher = r.matcher(noyear);
                    if (matcher.find()) {
                        day = matcher.group();
                    } else {
                        return tmp;
                    }
                }
                int m=getMonthFromStr(tmp);
                if(m==-1){
                    return tmp;
                }

                int y = Calendar.getInstance().get(Calendar.YEAR);
                pattern = ""+(y-1);
                r = Pattern.compile(pattern);
                matcher = r.matcher(tmp);
                if (matcher.find()) {
                    y--;
                }
                int d = Integer.parseInt(day);
                Calendar calendar = new GregorianCalendar(y, m - 1, d);
                String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRANCE);
                String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRANCE);
                formattedDate = dayOfWeek + " " + d + " " + month;
            }
            if(formattedDate==null){
                return tmp;
            }
            sb.append(formattedDate);
            return sb.toString();
        } catch (Exception e){
            //do nothing
        }

        return tmp;
    }

    private void appendPodcastType(String url, StringBuilder sb) {
        if (url.contains("gral") || url.contains("les-grosses")) {
            sb.append("L'intégrale du ");
        } else if (url.contains("pite")) {
            sb.append("Les pépites du ");
        } else if (url.contains("of")) {
            sb.append("Le best of du ");
        } else if (url.contains("yst")) {
            sb.append("L'invité mystère du ");
        } else {
            sb.append("Les grosses têtes du ");
        }
    }

    private boolean matchDate1(String tmpDate) {
        String pattern = "\\d{6}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(tmpDate);
        if(matcher.find()) {
            formattedDate = getDateFrom6Num(matcher.group());
            return true;
        }
        return false;
    }
    private boolean matchDate2(String tmpDate) {
        String pattern = "\\d{2}\\s\\d{2}\\s\\d{4}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(tmpDate);
        if(matcher.find()) {
            String date = matcher.group().replaceAll(" ","");
            date = date.substring(0,4)+date.substring(6);
            formattedDate = getDateFrom6Num(date);
            return true;
        }
        return false;
    }
    private boolean matchDate3(String tmpDate) {
        String pattern = "\\d{2}\\s\\d{2}\\s\\d{2}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(tmpDate);
        if(matcher.find()) {
            formattedDate = getDateFrom6Num(matcher.group().replaceAll(" ",""));
            return true;
        }
        return false;
    }



    /**
     *
     * @param tmp the podcast title
     * @return the number of the month contained in the string
     */
    private int getMonthFromStr(String tmp) {
        int m;
        if (tmp.contains("anvier")) {
            m = 1;
        } else if (tmp.contains("vrier")) {
            m = 2;
        } else if (tmp.contains("ars")) {
            m = 3;
        } else if (tmp.contains("vril")) {
            m = 4;
        } else if (tmp.toLowerCase().contains("mai")) {
            m = 5;
        } else if (tmp.contains("uin")) {
            m = 6;
        } else if (tmp.contains("uillet")) {
            m = 7;
        } else if (tmp.contains("out") || tmp.contains("oût")) {
            m = 8;
        } else if (tmp.contains("eptembre")) {
            m = 9;
        } else if (tmp.contains("ctobre")) {
            m = 10;
        } else if (tmp.contains("ovembre")) {
            m = 11;
        } else if (tmp.contains("cembre")) {
            m = 12;
        } else {
            return -1;
        }
        return m;
    }

    /**
     *
     * @param nums the date in number format eg:060215
     * @return the date in string format
     */
    private String getDateFrom6Num(String nums){
        int year_int = Calendar.getInstance().get(Calendar.YEAR);
        String year = "" + (year_int % 100);
        String year_old = "" + ((year_int % 100) - 1);
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
            return null;
        }
        Calendar calendar = new GregorianCalendar(y, m - 1, d);
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRANCE);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.FRANCE);
        return dayOfWeek + " " + d + " " + month;
    }

    /**
     * Gets the image resource depending on the podcast title
     *
     * @param title the title of the podcast
     * @return the image to display
     */
    private String getImg(String title) {
        String res = String.valueOf(gtlr);
        if (title.contains("intégrale")) {
            res = String.valueOf(gtlr);
        } else if (title.contains("pépite")) {
            res = String.valueOf(gold);
        } else if (title.contains("of")) {
            res = String.valueOf(best_of);
        } else if (title.contains("yst")) {
            res = String.valueOf(anonymous);
        }
        return res;
    }

    /**
     * Gets the day depending on the podcast title
     *
     * @param title the title of the podcast
     * @return the day of the week contained in the title
     */
    private String getDay(String title) {
        String res = "";
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
    public void updateLayout() {
        ArrayList<HashMap<String, String>> list = act.getListItem();
        ArrayList<HashMap<String, String>> newList = new ArrayList<>();
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        boolean integ = sharedPref.getBoolean("integral",true);
        boolean best = sharedPref.getBoolean("best",true);
        boolean mom = sharedPref.getBoolean("moments",true);
        boolean guest = sharedPref.getBoolean("guest",true);
        for(HashMap<String,String> item : list){
            if((item.get("img").equals(String.valueOf(gold)) && mom) ||
                    (item.get("img").equals(String.valueOf(gtlr)) && integ) ||
                    (item.get("img").equals(String.valueOf(best_of)) && best) ||
                    (item.get("img").equals(String.valueOf(anonymous)) && guest)
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