package com.bidgely.energybuddy.dto.response;

public class Response {

    private OutputSpeech outputSpeech;
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

    public boolean isShouldEndSession() {
        return shouldEndSession;
    }

    public void setShouldEndSession(boolean shouldEndSession) {
        this.shouldEndSession = shouldEndSession;
    }
}
