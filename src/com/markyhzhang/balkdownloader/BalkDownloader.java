package com.markyhzhang.balkdownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
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
        new BalkDownloader().oneClassicalDotComDownloader();
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
}
