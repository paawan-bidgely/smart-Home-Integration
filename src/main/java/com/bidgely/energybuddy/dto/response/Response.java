package com.bidgely.energybuddy.dto.response;

public class Response {

    private OutputSpeech outputSpeech;
    private Card card;
    private boolean shouldEndSession;

    public Response() {
    }

    public Response(String text, boolean shouldEndSession) {
        this.outputSpeech = new OutputSpeech(text);
        this.shouldEndSession = shouldEndSession;
    }

    public OutputSpeech getOutputSpeech() {
        return outputSpeech;
    }

    public void setOutputSpeech(OutputSpeech outputSpeech) {
        this.outputSpeech = outputSpeech;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean isShouldEndSession() {
        return shouldEndSession;
    }

    public void setShouldEndSession(boolean shouldEndSession) {
        this.shouldEndSession = shouldEndSession;
    }
}
