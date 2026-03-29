package com.bidgely.energybuddy.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlexaRequest {

    private String version;
    private Session session;
    private RequestBody request;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public RequestBody getRequest() {
        return request;
    }

    public void setRequest(RequestBody request) {
        this.request = request;
    }
}
