package com.waze.request;

import java.util.List;

public class WazeTrafficNotificationsResponse {
    private List<WazeAlert> alertLists;

    public void setAlerts(List<WazeAlert> alerts) {
        this.alertLists = alerts;
    }

    public List<WazeAlert> getWazeAlerts() {
        return this.alertLists;
    }
}