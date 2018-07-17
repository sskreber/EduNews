package com.example.android.edunews;

public class News {

    private String title;
    private String section;
    private String publicationDate;
    private String url;

    // its presence only optional in json:
    private String authorOrSource;

    public News() {
    }

    public News(String title, String section, String publicationDate, String url) {
        this.title = title;
        this.section = section;
        this.publicationDate = publicationDate;
        this.url = url;
    }

    public News(String title, String section, String publicationDate, String url, String authorOrSource) {
        this.title = title;
        this.section = section;
        this.publicationDate = publicationDate;
        this.url = url;
        this.authorOrSource = authorOrSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorOrSource() {
        return authorOrSource;
    }

    public void setAuthorOrSource(String authorOrSource) {
        this.authorOrSource = authorOrSource;
    }
}
