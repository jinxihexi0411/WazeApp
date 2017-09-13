package com.waze.service;

import com.waze.request.*;
import com.waze.utils.Utils;
import com.waze.exception.WazeException;

import com.ning.http.client.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WazeNotificationService {
    ObjectMapper mapper = new ObjectMapper();
    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public WazeNotificationService() {}

    private static String genNotificationUrl(String server, String left, String right, String top, String bottom) {
        String url = "https://www.waze.com/" + server + "/web/TGeoRSS?left="
                + left + "&right=" + right + "&bottom=" + bottom + "&top=" + top;
        return url;
    }

    //Longitude X-left  Longitude X-right latitude Y-top, latitude Y-bottom
    public WazeTrafficNotificationsResponse getNotifications(String left, String right, String top, String bottom) {
        String[] serverList = {"rtserver", "row-rtserver", "il-rtserver"};
        String url = "";
        WazeTrafficNotificationsResponse wazeTrafficNotificationsResponse = new WazeTrafficNotificationsResponse();
        List<WazeAlert> alerts = new ArrayList<>();

        try{
            for (String server: serverList){
                url = genNotificationUrl(server, left, right, top, bottom);
                String responseStr = asyncHttpClient
                        .prepareGet(url)
                        .execute()
                        .get()
                        .getResponseBody();

                if (!responseStr.isEmpty()){
                    JsonNode result = mapper.readTree(responseStr);
                    if (result.has("alerts")){
                        JsonNode alertsNode = result.get("alerts");
                        for (JsonNode alert: alertsNode){
                            String country = Utils.getStringOrNull("country", alert);
                            int numOfThumbsUp = Utils.getIntOrNull("nThumbsUp", alert);
                            String type = Utils.getStringOrNull("type", alert);
                            String subType = Utils.getStringOrNull("subtype", alert);
                            String placeNearBy = Utils.getStringOrNull("nearBy", alert);
                            String latitude = Utils.getX2StringOrNull("location", "y", alert);
                            String longitude = Utils.getX2StringOrNull("location", "x", alert);
                            WazeAlert wazeAlert = new WazeAlert(country, numOfThumbsUp, type, subType, placeNearBy, latitude, longitude);
                            alerts.add(wazeAlert);
                        }
                    }
                }
            }
        }catch (Exception ex) {
            throw new RuntimeException("failed to get notifications \nurl: "+ url + "\nerror: "+ ex.getMessage());
        }

        wazeTrafficNotificationsResponse.setAlerts(alerts);
        return wazeTrafficNotificationsResponse;
    }

    public JsonNode getAddress(String address){
        String addressURL = "";
        try {
            addressURL = "https://www.waze.com/SearchServer/mozi?q=" + URLEncoder.encode(address, "UTF-8") + "&lang=eng&lon=-73.96888732910156%20&lat=40.799981900731964&origin=livemap";
            Response response = asyncHttpClient
                    .prepareGet(addressURL)
                    .execute().get();
            return mapper.readTree(response.getResponseBody()).get(0);
        }catch (Exception ex) {
            throw new WazeException("failed to query waze address \nurl: " + addressURL + " \nerror: "+ ex.getMessage());
        }
    }
}
