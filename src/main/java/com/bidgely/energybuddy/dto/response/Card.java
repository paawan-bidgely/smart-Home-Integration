package com.bidgely.energybuddy.dto.response;

public class Card {

    private String type;
    private String title;
    private String content;

    public Card() {
    }

    public Card(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
