package com.ravcube.lib.event.nats;

public class NatsProperties {

    private String url = "nats://localhost:4222";
    private String subjectPrefix = "application";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }
}
