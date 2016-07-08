package de.kja.app.model;

public class Content {

    private long id;
    private String title;
    private String shortText;
    private String text;

    public Content() {

    }

    public Content(long id, String title, String shortText, String text) {
        this.id = id;
        this.title = title;
        this.shortText = shortText;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortText() {
        return shortText;
    }

    public String getText() {
        return text;
    }
}
