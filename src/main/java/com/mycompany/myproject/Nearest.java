package com.mycompany.myproject;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.Arrays.stream;

public class Nearest extends Verticle {

    private NearestImpl nearest;

    public void start() {
        InputStream stoppoints = getClass().getResourceAsStream("/stoppoints.csv");
        InputStream sites = getClass().getResourceAsStream("/sites.csv");
        if (stoppoints != null) {
            nearest = new NearestImpl(
                    new BufferedReader(new InputStreamReader(stoppoints)).lines(),
                    new BufferedReader(new InputStreamReader(sites)).lines());
        }

        vertx.eventBus().registerHandler(
                "nearest",
                (Message<String> m) ->
                        m.reply(nearest.get(stream(m.body().split(",")).mapToDouble(Double::parseDouble).toArray())));
    }
}
