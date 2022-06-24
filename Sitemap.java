import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Sitemap {

    private static List<Link> links = new ArrayList<>();
    private static SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
    private static String footer = "</urlset>";
    private static String body = "<url>\n" +
    "  <loc>%1$s</loc>\n" +
    "  <lastmod>%2$s</lastmod>\n" +
    "</url>\n";
    private static StringBuilder sitemap = new StringBuilder();

    public static void main(String[] args) {
        //String domain = args[0];
        //String url = args[1];
        String url = "/home/mrepol742/VSCodeProjects/mrepol742.github.io";
        String domain = "https://mrepol742.github.io/";
        find(new File(url), domain);

        sitemap.append(header);
        for (Link link: links) {
            sitemap.append(String.format(body, link.url, link.date));
        }
        sitemap.append(footer);
        System.out.println(sitemap.toString());
    }

    public static void find(File file, String domain) {
        String[] listFiles = file.list();
        for (String str: listFiles) {
            File folder = new File(file.getAbsolutePath() + "/" + str);
            if (folder.isDirectory()) {
                File hasIndex = new File(folder.getAbsolutePath() + "/index.html");
                if (hasIndex.isFile()) {
                    long lastModified = file.lastModified();
                    Date date = new Date(lastModified);
                    // System.out.println("the https://mrepol742.github.io/" + str + "/ can be added to sitemap modified on " + format.format(date));
                    links.add(new Link(domain + str + "/", format.format(date)));
                }
            }
        
        }
    }
}

class Link {
    public String url;
    public String date;

    public Link(String url, String date) {
        this.url = url;
        this.date = date;
    }
}