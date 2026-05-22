package com.taskmanager.dto.websocket;

public class ClientSubscribeMessage {
    // private String type; # NOT SUPPORTED for MVP
    private Long interval;

    public ClientSubscribeMessage() {
    }

    // public ClientSubscribeMessage(String type, Long interval) {
    //     this.type = type;
    //     this.interval = interval;
    // }


    public ClientSubscribeMessage(Long interval) {
        this.interval = interval;
    }

    // public String getType() {
    //     return type;
    // }

    // public void setType(String type) {
    //     this.type = type;
    // }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }
}
