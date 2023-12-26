package com.mrepol742.sitemapgenerator;

class Arg {
    String domain;
    String publisher;
    String projectFolder;

    public Arg() {
        this.projectFolder = System.getProperty("user.dir").replace("Sitemap-Generator", "");
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public String getProjectFolder() {
        return this.projectFolder;
    }
}