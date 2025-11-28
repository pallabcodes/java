package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EventRequest {

    @NotBlank(message = "Event data cannot be blank")
    @Size(max = 10000, message = "Event data cannot exceed 10000 characters")
    private String eventData;

    public EventRequest() {}

    public EventRequest(String eventData) {
        this.eventData = eventData;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    @Override
    public String toString() {
        return "EventRequest{" +
                "eventData='" + eventData + '\'' +
                '}';
    }
}
