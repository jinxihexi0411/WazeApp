package com.waze;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.JsonNode;
import com.waze.service.WazeNotificationService;

public class WazeHealthCheck extends HealthCheck {

    private final WazeNotificationService wazeRouteService;

    public WazeHealthCheck(WazeNotificationService wazeNotificationService) {
        this.wazeRouteService = wazeNotificationService;
    }

    @Override
    protected Result check() throws Exception {
        JsonNode addressResult = wazeRouteService.getAddress("5th avenue new york");
        String addressResultStr = addressResult.get("name").asText();
        if (!addressResultStr.contains("5th")) {
            return Result.unhealthy("waze api failed to query waze web site");
        }
        return Result.healthy();
    }

}