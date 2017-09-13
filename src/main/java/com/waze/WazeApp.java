package com.waze;

import com.waze.service.WazeNotificationService;
import com.waze.controller.WazeController;

import com.fasterxml.jackson.databind.SerializationFeature;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.meltmedia.dropwizard.mongo.MongoBundle;

import java.io.IOException;


public class WazeApp extends Application<WazeConfig> {
    MongoBundle<WazeConfig> mongoBundle;

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        new WazeApp().run(args);
    }

    @Override
    public String getName() {
        return "waze-app";
    }

    @Override
    public void initialize(Bootstrap<WazeConfig> bootstrap) {
        bootstrap.addBundle(mongoBundle =
                MongoBundle.<WazeConfig> builder()
                           .withConfiguration(WazeConfig::getMongo)
                           .build());
    }

    @Override
    public void run(WazeConfig config, Environment env) throws IOException {
             env.getObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        final WazeNotificationService wazeNotificationService = new WazeNotificationService();
        final WazeController wazeController = new WazeController(wazeNotificationService, mongoBundle.getDB());
        final WazeHealthCheck wazeHealthCheck = new WazeHealthCheck(wazeNotificationService);

        env.healthChecks().register("waze", wazeHealthCheck);
        env.jersey().register(wazeController);
    }
}