package com.waze.controller;

import com.codahale.metrics.annotation.Timed;

import com.waze.request.WazeAlert;
import com.waze.request.WazeTrafficNotificationsResponse;
import com.waze.exception.WazeException;
import com.waze.service.WazeNotificationService;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mongodb.DB;
import org.jongo.Jongo;

import java.util.List;

@Path("/waze")
@Produces(MediaType.APPLICATION_JSON)
public class WazeController {
    private final WazeNotificationService wazeNotificationService;
    private final Logger log = LoggerFactory.getLogger("waze");
    DB database;
    Jongo jongo;
    MongoCollection alretCol;

    public WazeController(WazeNotificationService wazeNotificationService, DB database) {
        this.wazeNotificationService = wazeNotificationService;
        this.database = database;
        this.jongo = new Jongo(database);
        this.alretCol = jongo.getCollection("alerts");
    }

    @GET
    @Timed
    @Path("/traffic-notifications")
    public WazeTrafficNotificationsResponse notifications(
            @QueryParam("lonLeft") String lonLeft,
            @QueryParam("lonRight") String lonRight,
            @QueryParam("latTop") String latTop,
            @QueryParam("latBottom") String latBottom) {

        try {
            WazeTrafficNotificationsResponse response = wazeNotificationService.getNotifications(lonLeft, lonRight, latTop, latBottom);
            List<WazeAlert> alertLists = response.getWazeAlerts();
            for (WazeAlert alert : alertLists) {
                alretCol.insert(alert);
            }
            return response;
        } catch (Exception ex) {
            log.error("failed to notifications: " + ex.getMessage());
            throw new WazeException("failed to get notifications");
        }
    }
}