package com.markyhzhang.balkdownloader;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BulkDownloader
 *
 * @author Mark (Yi Han) Zhang
 * @since 2017-06-25
 */
public class BalkDownloader {

    public static final String DOWNLOAD_PATH="";
    private static final String[] COMPOSERS = {"bach","brahms","chopin","dvorak","handel","haydn","liszt","rachmaninov","schubert","schumann","strauss","tchaikovsky","vivaldi","wagner"};
//    private static final String[] COMPOSERS = {"chopin"};

    private static int startOn = 0;

    public static void main(String[] args) {
        startOn = Integer.parseInt(args[0]);
        System.out.println(startOn);
        new BalkDownloader().pianoSocietyDownloader();

        try {
            PrintWriter pw = new PrintWriter(new File("errorLog.txt"));
            for (String badURL : errorLog) {
                pw.println(badURL);
            }
            pw.close();
        }catch (Exception e){

        }
//        System.out.println(errorLog);
//        new BalkDownloader().test();
    }

    public void pianoParadiseDownloader(){
        try{
            for (String composer : COMPOSERS) {
                String rawHTML = new Scanner(new URL("http://www.pianoparadise.com/downloadmp3/"+composer+".html").openStream(),"UTF-8").useDelimiter("\\A").next();
                Scanner sc = new Scanner(rawHTML);
                while (sc.hasNext()){
                    String line = sc.next();
                    if (line.startsWith("href=\"http://www.pianoparadise.com/downloadmp3/")&&!line.contains("html")){
                        String link = line.substring(6).split("\"")[0];

                        URL website = new URL(link);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        FileOutputStream fos = new FileOutputStream(DOWNLOAD_PATH+composer+link.split("")[link.split("/").length-1]);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void oneClassicalDotComDownloader(){
        String rootURL="http://1classical.com/";

        int count = 0;

        try {
            String rawComposersHTML = new Scanner(new URL("http://1classical.com/download_free_classical_music_MP3_browse_by_composer.php").openStream(), "UTF-8").useDelimiter("\\A").next();
            Scanner sc = new Scanner(rawComposersHTML);
            while (sc.hasNext()) {
                String rawStr = sc.next();
                if (rawStr.startsWith("href=\"composer.php?composer=")) {
                    String composerPageURL = rootURL + rawStr.substring(6).split("\"")[0];
                    String composerName = composerPageURL.split("=")[1];

                    File theDir = new File(composerName);
                    if (!theDir.exists()) {
                        try {
                            theDir.mkdir();
                        } catch (SecurityException se) {
                        }
                    }

                    String rawCurComposerHTML = new Scanner(new URL(composerPageURL).openStream(), "UTF-8").useDelimiter("\\A").next();

                    String albumRegex = Pattern.quote("recordID=") + "(.*?)" + Pattern.quote("\">");
                    Pattern albumPattern = Pattern.compile(albumRegex);
                    Matcher albumMatcher = albumPattern.matcher(rawCurComposerHTML);

                    while (albumMatcher.find()) {
                        String albumURL = ("http://1classical.com/title.php?recordID=" + albumMatcher.group(1)).replace(" ", "%20").replace("\'", "%27");
                        String rawAlbumHTML = new Scanner(new URL(albumURL).openStream(), "UTF-8").useDelimiter("\\A").next();

                        String songRegex = Pattern.quote("<a href=\"http://www.scientificinvesting.eu/a/") + "(.*?)" + Pattern.quote(".mp3\" onmouse");
                        Pattern songPattern = Pattern.compile(songRegex);
                        Matcher songMatcher = songPattern.matcher(rawAlbumHTML);

                        String pre = "";
                        while (songMatcher.find()) {
                            String rawSongName = songMatcher.group(1);
                            String songURL = ("http://www.scientificinvesting.eu/a/" + rawSongName).replace(" ", "%20").replace("\'", "%27") + ".mp3";
                            if (songURL.equals(pre)) continue;
                            pre = songURL;

                            count++;

                            if (count < startOn) {
                                System.out.println("Skipping #" + count);
                                continue;
                            }

                            try {
                                URL website = new URL(songURL);
                                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                FileOutputStream fos = new FileOutputStream(composerName + "/" + rawSongName + ".mp3");
                                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                                System.out.println("#" + count + " " + rawSongName);
                            } catch (Exception e) {
                                System.out.println("[!BAD!]#" + count + " " + rawSongName);
                            }
                        }
                    }
                } else if (rawStr.startsWith("href=\"composer_type.php?composer=")) {
                    String composerTypePageURL = rootURL + rawStr.substring(6).split("\"")[0];
                    String composerName = composerTypePageURL.split("=")[1];

                    File theDir = new File(composerName);
                    if (!theDir.exists()) {
                        try {
                            theDir.mkdir();
                        } catch (SecurityException se) {
                        }
                    }

                    String rawCurComposerTypeHTML = new Scanner(new URL(composerTypePageURL).openStream(), "UTF-8").useDelimiter("\\A").next();

                    String curComposerRegex = Pattern.quote("composer.php?composer=") + "(.*?)" + Pattern.quote("\">");
                    Pattern curComposerPattern = Pattern.compile(curComposerRegex);
                    Matcher curComposerMatcher = curComposerPattern.matcher(rawCurComposerTypeHTML);

                    while (curComposerMatcher.find()) {
                        String composerPageURL = rootURL + "composer.php?composer=" + curComposerMatcher.group(1).replace(" ", "%20").replace("\'", "%27");

                        String rawCurComposerHTML = new Scanner(new URL(composerPageURL).openStream(), "UTF-8").useDelimiter("\\A").next();

                        String albumRegex = Pattern.quote("recordID=") + "(.*?)" + Pattern.quote("\">");
                        Pattern albumPattern = Pattern.compile(albumRegex);
                        Matcher albumMatcher = albumPattern.matcher(rawCurComposerHTML);

                        while (albumMatcher.find()) {
                            String albumURL = ("http://1classical.com/title.php?recordID=" + albumMatcher.group(1)).replace(" ", "%20").replace("\'", "%27");
                            String rawAlbumHTML = new Scanner(new URL(albumURL).openStream(), "UTF-8").useDelimiter("\\A").next();

                            String songRegex = Pattern.quote("<a href=\"http://www.scientificinvesting.eu/a/") + "(.*?)" + Pattern.quote(".mp3\" onmouse");
                            Pattern songPattern = Pattern.compile(songRegex);
                            Matcher songMatcher = songPattern.matcher(rawAlbumHTML);

                            String pre = "";
                            while (songMatcher.find()) {
                                String rawSongName = songMatcher.group(1);
                                String songURL = ("http://www.scientificinvesting.eu/a/" + rawSongName).replace(" ", "%20").replace("\'", "%27") + ".mp3";
                                if (songURL.equals(pre)) continue;
                                pre = songURL;

                                count++;

                                if (count < startOn) {
                                    System.out.println("Skipping #" + count);
                                    continue;
                                }

                                try {
                                    URL website = new URL(songURL);
                                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                    FileOutputStream fos = new FileOutputStream(composerName + "/" + rawSongName + ".mp3");
                                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                                    System.out.println("#" + count + " " + rawSongName);
                                } catch (Exception e) {
                                    System.out.println("[!BAD!]#" + count + " " + rawSongName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1001pianos
    private void oneOOOnePianosDownloader(){
        String rootURL = "http://www.1001pianos.com/audio/by/artist/";

        int count = 0;

        try {
            String rawComposersHTML = new Scanner(new URL(rootURL).openStream(), "UTF-8").useDelimiter("\\A").next();

            String composerRegex = Pattern.quote("<a href=\"/audio/by/artist/") + "(.*?)" + Pattern.quote("\">");
            Pattern composerPattern = Pattern.compile(composerRegex);
            Matcher composerMatcher = composerPattern.matcher(rawComposersHTML);

            while (composerMatcher.find()) {
                String composerName = composerMatcher.group(1).trim();
                if (composerName.equals("")) continue;
                String formatedComposerName = composerName.split("_")[composerName.split("_").length - 1];
                formatedComposerName = formatedComposerName.substring(0, 1).toUpperCase() + formatedComposerName.substring(1)+" (piano)";

                File theDir = new File(formatedComposerName);
                if (!theDir.exists()) {
                    try {
                        theDir.mkdir();
                    } catch (SecurityException se) {
                    }
                }

                String rawSongsHTML = new Scanner(new URL(rootURL + composerName).openStream(), "UTF-8").useDelimiter("\\A").next();

                String songsRegex = Pattern.quote("<a href=\"") + "(.*?)" + Pattern.quote(".mp3");
                Pattern songsPattern = Pattern.compile(songsRegex);
                Matcher songsMatcher = songsPattern.matcher(rawSongsHTML);

                while (songsMatcher.find()) {
                    String songURL = songsMatcher.group(1) + ".mp3";
                    String songName = songURL.split("/")[songURL.split("/").length - 1].replace("_", " ").replace("+"," ").replace("%23","#").replace("%2523","#").replace("%28","(").replace("%29",")").replace("%27","'");

                    count++;
                    if (count < startOn) {
                        System.out.println("Skipping #" + count);
                        continue;
                    }

                    try {
                        URL website = new URL(songURL);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        FileOutputStream fos = new FileOutputStream(formatedComposerName + "/" + songName);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        System.out.println("#" + count + " " + songName.substring(0,songName.length()-4));
                    } catch (Exception e) {
                        System.out.println("[!BAD!]#" + count + " " + songName.substring(0,songName.length()-4));
                    }
                }
            }
        }catch (Exception e){

        }
    }

    //http://www.pianosociety.com/pages/composers/
    static ArrayList<String> errorLog = new ArrayList<>();
    private void pianoSocietyDownloader(){
        String rootURL = "http://www.pianosociety.com/pages/";

        int count = 0;

        try {
            String rawComposersHTML = new Scanner(new URL(rootURL+"composers").openStream(), "UTF-8").useDelimiter("\\A").next();

            String composerRegex = Pattern.quote("<a href=\"pages/") + "(.*?)" + Pattern.quote("/\">");
            Pattern composerPattern = Pattern.compile(composerRegex);
            Matcher composerMatcher = composerPattern.matcher(rawComposersHTML);

            //AVOID USELESS PAGES
            for (int i = 0; i < 4; i++) {
                composerMatcher.find();
                composerMatcher.group(1);
            }



            //because there are duplicates THIS SHOULD BE FIXED TO AVOID HARD CODING
            for(int i = 0; i < 180; i++) {
                composerMatcher.find();
                String composerName = composerMatcher.group(1);
                String formatedComposerName = composerName.substring(0, 1).toUpperCase() + composerName.substring(1);

                File theDir = new File(formatedComposerName);
                if (!theDir.exists()) {
                    try {
                        theDir.mkdir();
                    } catch (SecurityException se) {
                    }
                }

                String rawAlbumsHTML = new Scanner(new URL(rootURL + composerName).openStream(), "UTF-8").useDelimiter("\\A").next();

                String albumsRegex = Pattern.quote("class=\"childNode\"><a href=\"pages") + "(.*?)" + Pattern.quote("/\">");
                Pattern albumsPattern = Pattern.compile(albumsRegex);
                Matcher albumsMatcher = albumsPattern.matcher(rawAlbumsHTML);

                ArrayList<String> visited = new ArrayList<>();

                while (albumsMatcher.find()) {
                    String rawAlbum = albumsMatcher.group(1);
                    if (visited.contains(rawAlbum)) continue;
                    else visited.add(rawAlbum);

                    String rawSongHTML = new Scanner(new URL(rootURL + rawAlbum).openStream(), "UTF-8").useDelimiter("\\A").next();
                    String songRegex = Pattern.quote("<a href=\"http://") + "(.*?)" + Pattern.quote(".mp3");
                    Pattern songPattern = Pattern.compile(songRegex);
                    Matcher songMatcher = songPattern.matcher(rawSongHTML);

                    while (songMatcher.find()) {
                        String songURL = "NULL";
                        try {
                            String rawURL = songMatcher.group(1).trim();
                            songURL = "http://" + rawURL + ".mp3";

                            String rawName = rawURL.split("/")[rawURL.split("/").length-1];
                            String[] nameArr = rawName.split("-");
                            String formattedName = nameArr[0].substring(0,1).toUpperCase()+nameArr[0].substring(1)+" ";
                            for (int j = 1; j < nameArr.length-1; j++) {
                                formattedName+=nameArr[j].toUpperCase()+" ";
                            }
                            formattedName+=nameArr[nameArr.length-1].substring(0,1).toUpperCase()+nameArr[nameArr.length-1].substring(1);

                            count++;
                            if (count < startOn) {
                                System.out.println("Skipping #" + count);
                                continue;
                            }

                            try {
                                URL website = new URL(songURL);
                                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                FileOutputStream fos = new FileOutputStream(formatedComposerName + "/" + formattedName+".mp3");
                                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                                System.out.println("#" + count + " " + formattedName+".mp3");
                            } catch (Exception e) {
                                System.out.println("[!BAD!]#" + count + " " + formattedName + " " +songURL);
                                e.printStackTrace();
                                errorLog.add("[!BAD INSIDE!]#" + count + " " + formattedName + " " +songURL);
                                errorLog.add(stackTraceToString(e));
                            }
                        } catch (Exception e) {
                            System.out.println("[!BAD!]# " + count + " " + songURL);
                            e.printStackTrace();
                            errorLog.add("[!BAD OUTSIDE!]# " + count + " " + songURL);
                            errorLog.add(stackTraceToString(e));
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void test() {
        try {
            URL website = new URL("http://www.pianosociety.com/protected/beethoven-woo13-05-richards.mp3");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream("D:\\ComputerScience\\JavaProjects\\BulkDownloader\\out\\artifacts\\BalkDownloader\\test.mp3");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {

        }
    }
}
