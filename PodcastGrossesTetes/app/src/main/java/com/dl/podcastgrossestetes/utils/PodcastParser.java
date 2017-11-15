package com.dl.podcastgrossestetes.utils;

import com.dl.podcastgrossestetes.model.Podcast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dl.podcastgrossestetes.R.drawable.anonymous;
import static com.dl.podcastgrossestetes.R.drawable.best_of;
import static com.dl.podcastgrossestetes.R.drawable.gold;
import static com.dl.podcastgrossestetes.R.drawable.gtlr;

public class PodcastParser implements PodcastParserInterface {

    private String formattedDate;


    public ArrayList<Podcast> parsePage(String RssString) {
        ArrayList<Podcast> listItem = new ArrayList<>();
        String[] lines = RssString.split("\n");
        int i = 0;
        int start, end;
        Podcast podcast;
        String currentLine;
        String title, url;
        HashSet<String> podcastsInList = new HashSet<>();
        while (i < lines.length) {
            currentLine = lines[i];
            if (currentLine.contains("enclosure url")) {
                start = currentLine.indexOf("http");
                end = currentLine.indexOf("\"", start);
                url = currentLine.substring(start, end);
                title = getTitleFromUrl(url);
                if (podcastsInList.contains(title)) {
                    i++;
                    continue;
                }
                podcastsInList.add(title);
                final String type = getType(title);
                podcast = new Podcast(getDay(title), title, getImg(type), url, type);
                listItem.add(podcast);
            }
            i++;
        }
        return listItem;
    }


    /**
     * Makes a formatted title from the raw url
     *
     * @param url the url
     * @return formatted string
     */
    private String getTitleFromUrl(String url) {
        String tmp;
        StringBuilder sb = new StringBuilder();
        appendPodcastType(url, sb);
        tmp = (url.substring(url.indexOf("_") + 1, url.length() - 4)).replace("-", " ");
        try {
            if (!matchDate1(tmp) && !matchDate2(tmp) && !matchDate3(tmp)) {
                //if date is like "du 10 juillet"
                String noyear = tmp;
                if (tmp.contains("201")) {
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
                int m = getMonthFromStr(tmp);
                if (m == -1) {
                    return tmp;
                }

                int y = Calendar.getInstance().get(Calendar.YEAR);
                pattern = "" + (y - 1);
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
            if (formattedDate == null) {
                return tmp;
            }
            sb.append(formattedDate);
            return sb.toString();
        } catch (Exception e) {
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
        if (matcher.find()) {
            formattedDate = getDateFrom6Num(matcher.group());
            return true;
        }
        return false;
    }

    private boolean matchDate2(String tmpDate) {
        String pattern = "\\d{2}\\s\\d{2}\\s\\d{4}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(tmpDate);
        if (matcher.find()) {
            String date = matcher.group().replaceAll(" ", "");
            date = date.substring(0, 4) + date.substring(6);
            formattedDate = getDateFrom6Num(date);
            return true;
        }
        return false;
    }

    private boolean matchDate3(String tmpDate) {
        String pattern = "\\d{2}\\s\\d{2}\\s\\d{2}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(tmpDate);
        if (matcher.find()) {
            formattedDate = getDateFrom6Num(matcher.group().replaceAll(" ", ""));
            return true;
        }
        return false;
    }


    /**
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
     * @param nums the date in number format eg:060215
     * @return the date in string format
     */
    private String getDateFrom6Num(String nums) {
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
     * @param type the type of the podcast
     * @return the image to display
     */
    private int getImg(String type) {
        switch (type) {
            case "Best of":
                return best_of;
            case "Intégrale":
                return gtlr;
            case "Invité mystère":
                return anonymous;
            case "Pépites":
                return gold;
        }
        return gtlr;
    }

    private String getType(String title) {
        String res = "Intégrale";
        if (title.contains("intégrale")) {
            res = "Intégrale";
        } else if (title.contains("pépite")) {
            res = "Pépites";
        } else if (title.contains("of")) {
            res = "Best of";
        } else if (title.contains("yst")) {
            res = "Invité mystère";
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
        title = title.toLowerCase(Locale.FRENCH);
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