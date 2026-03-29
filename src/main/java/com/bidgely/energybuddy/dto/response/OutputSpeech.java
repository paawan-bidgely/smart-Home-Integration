package com.bidgely.energybuddy.dto.response;

public class OutputSpeech {

    private String type = "PlainText";
    private String text;

    public OutputSpeech() {
    }

    public OutputSpeech(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
