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
    static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><?xml-stylesheet type=\"text/xsl\" href=\"/css/sitemap.xsl\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:video=\"http://www.google.com/schemas/sitemap-video/1.1\" xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">\n";
    static String footer = "</urlset>";
    static StringBuilder sitemap = new StringBuilder();
    static Arg arg;
    static boolean isHome = false;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("arguments:\n\t--domain [address]\n\t--publisher [name]\noptional:\n\t--projectFolder [location]");
            return;
        }

        arg = resolveArguments(args); 

        find(new File(arg.getProjectFolder()), arg.getDomain());

        sitemap.append(header);
        for (Link link: links) {
            sitemap.append(String.format("<url>\n<loc>%1$s</loc>\n<lastmod>%2$s</lastmod>\n", link.url, link.date));
            sitemap.append(link.more);
            sitemap.append("</url>\n");
        }
        sitemap.append(footer);
          Files.createDirectories(Paths.get(arg.getProjectFolder() + "/sitemap"));
        if (write(new File(arg.getProjectFolder() + "/sitemap/index.xml"), sitemap.toString(), false)) {
            System.out.println("\nSitemap generated for " + arg.getDomain());
        } else {
            System.out.println("\nFailed to generate sitemap.");
        }
    }

    public static String getImages(File file) {
        StringBuilder images = new StringBuilder();
        Document doc = Jsoup.parse(read(file, "\n"));
        Elements image = doc.getElementsByTag("img");
        for (Element el : image) {
            String src = el.attr("src");
            if (src.startsWith("/images/")) {
                images.append("  <image:image>\n    <image:loc>" + arg.getDomain() + src.replaceAll("/images","images") + "</image:loc>\n  </image:image>");
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
                videos.append("  <video:video>\n    <video:title>" + (src.replaceAll("%20", " ").replaceAll("/videos/","").replaceAll(".mp4", ""))+ "</video:title>\n    <video:content_loc>" + arg.getDomain() + src.replace("/videos","videos") + "</video:content_loc>\n  </video:video>");
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
                     System.out.println(format.format(hasIndex.lastModified()) + " | " + arg.getDomain() + hasIndex.getParentFile().getAbsolutePath().replace(arg.getProjectFolder(), ""));
                    links.add(new Link(arg.getDomain() + hasIndex.getParentFile().getAbsolutePath().replace(arg.getProjectFolder(), "") , format.format(hasIndex.lastModified()), getImages(hasIndex) + "\n" + getVideos(hasIndex)));
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