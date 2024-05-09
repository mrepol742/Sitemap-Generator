package com.mrepol742.sitemapgenerator;

import java.text.SimpleDateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.nio.file.Paths;
import java.nio.file.Files;


import org.jsoup.*;
import org.jsoup.select.*;
import org.jsoup.helper.*;
import org.jsoup.internal.*;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;

public class App {

    static List<Link> links = new ArrayList<>();
    static SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:video=\"http://www.google.com/schemas/sitemap-video/1.1\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">\n";
    static String footer = "</urlset>";
    static StringBuilder sitemap = new StringBuilder();
    static Arg arg;
    static boolean isHome = false;
    static long timeStarted = 0;

    public static void main(String[] args) throws IOException {
        timeStarted = System.currentTimeMillis();
        if (args.length == 0) {
            System.out.println("arguments:\n\t--domain [address]\n\t--publisher [name]\noptional:\n\t--projectFolder [location]");
            return;
        }

        arg = resolveArguments(args); 

        System.out.println("\n\nScanning for files...");
        System.out.println("----------< " + arg.getDomain() + " >----------\nBuilding /Sitemap/index.xml\n");

        find(new File(arg.getProjectFolder()), arg.getDomain());

        sitemap.append(header);
        for (Link link: links) {
            sitemap.append(String.format("<url>\n\t<loc>%1$s</loc>\n\t<lastmod>%2$s</lastmod>\t", link.url, link.date));
            sitemap.append(link.more);
            sitemap.append("</url>\n");
        }
        sitemap.append(footer);
        Files.createDirectories(Paths.get(arg.getProjectFolder() + "/sitemap"));
        StringBuilder sb = new StringBuilder("\n------------------------------------------------------------------------");
        if (write(new File(arg.getProjectFolder() + "/sitemap/index.xml"), sitemap.toString(), false)) {
            sb.append("\nBuild Success");
        } else {
            sb.append("\nBuild Failed");
        }
        sb.append("\n------------------------------------------------------------------------");
        long sum = System.currentTimeMillis() - timeStarted;
        sb.append("\nTotal time: ");
        sb.append(String.valueOf(sum));
        sb.append(" s");
        sb.append("\nFinished at: ");
        sb.append(format.format(System.currentTimeMillis()));
        sb.append("\n------------------------------------------------------------------------");
        System.out.println(sb.toString());
    }

    public static String getImages(File file) {
        StringBuilder images = new StringBuilder();
        Document doc = Jsoup.parse(read(file, "\n"));
        Elements image = doc.getElementsByTag("img");
        for (Element el : image) {
            String src = el.attr("src");
            if (src.startsWith("/images/")) {
                String location = arg.getDomain() + src.replaceAll("/images","images");
                System.out.println("\timage >> " + location);
                images.append("\n\t<image:image>\n\t\t<image:loc>" + location + "</image:loc>\n\t</image:image>");
            }
        }
        return images.toString();
    }

    public static String getVideos(File file) {
        StringBuilder videos = new StringBuilder();
        Document doc = Jsoup.parse(read(file, "\n"));
        Elements image = doc.getElementsByTag("source");
        for (Element el : image) {
            String src = el.attr("src");
            if (src.startsWith("/videos/")) {
                String location = arg.getDomain() + src.replace("/videos","videos");
                System.out.println("\tvideo >> " + location);
                videos.append("\t<video:video>\n\t\t<video:title>" + (src.replaceAll("%20", " ").replaceAll("/videos/","").replaceAll(".mp4", ""))+ "</video:title>\n\t\t<video:content_loc>" + location + "</video:content_loc>\n\t</video:video>\n");
            }
        }
        return videos.toString();
    }

    public static void find(File file, String domain) {
        if (file.list() == null) {
            System.out.println("no index " + file.toString());
            return;
        }
        if (file.isDirectory() && !isHome) {
            File root = new File(arg.getProjectFolder() + "/index.html");
            System.out.println(format.format(root.lastModified()) + " | " + arg.getDomain());
            links.add(new Link(arg.getDomain(), format.format(root.lastModified()), getImages(root) + "\n" + getVideos(root)));
            isHome = true;
        }
        String[] listFiles = file.list();
        for (String str: listFiles) {
            File folder = new File(file.getAbsolutePath() + "/" + str);
            if (folder.isDirectory()) {
                File hasIndex = new File(folder.getAbsolutePath() + "/index.html");
                if (hasIndex.isFile()) {
                    String location = arg.getDomain() + hasIndex.getParentFile().getAbsolutePath().replace(arg.getProjectFolder() + "/", "");
                    String lastMod = format.format(hasIndex.lastModified());
                    String mediaFiles = getImages(hasIndex) + "\n" + getVideos(hasIndex);
                    System.out.println(lastMod + " | " + location);
                    links.add(new Link(location, lastMod, mediaFiles));
                    find(new File (file.getAbsolutePath() + "/" + str), arg.getDomain());
                }
            }
        
        }
    }


    public static boolean write(File location, String data, boolean readOnly) {
        try {
            FileWriter fw = new FileWriter(location, false);
            fw.write(data);
            fw.close();
            if (readOnly) {
                boolean bn = location.setReadOnly();
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

       public static String read(java.io.File fe, String line) {
        try {
            if (!fe.exists()) {
                return null;
            }
            FileReader fr = new FileReader(fe);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = br.readLine()) != null) {
                sb.append(ln);
                sb.append(line);
            }
            fr.close();
            br.close();
            return sb.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

        public static Arg resolveArguments(String[] args) {
        Arg arg = new Arg();
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "--domain":
                    arg.setDomain(args[i + 1]);
                break;
                case "--publisher":
                    arg.setPublisher(args[i + 1]);
                break;
                case "--projectFolder":
                    arg.setProjectFolder(args[i + 1]);
                break;
            }
        }

        if (arg.getDomain() == null) {
            throw new RuntimeException("Undefined --domain value");
        }
        if (arg.getPublisher() == null) {
            throw new RuntimeException("Undefined --publisher value");
        }
        return arg;
    }
}