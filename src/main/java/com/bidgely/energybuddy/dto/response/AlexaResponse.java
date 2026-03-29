package com.bidgely.energybuddy.dto.response;

public class AlexaResponse {

    private String version = "1.0";
    private Response response;

    public AlexaResponse() {
    }

    public AlexaResponse(String speechText, boolean shouldEndSession) {
        this.response = new Response(speechText, shouldEndSession);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
