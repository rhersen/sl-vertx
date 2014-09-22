package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class Nearest extends Verticle {

    private NearestImpl nearest;

    public void start() {
        nearest = new NearestImpl();

        get("Sites", nearest::setSites);
        get("StopPoints", nearest::setStopPoints);

        vertx.eventBus().registerHandler(
                "nearest",
                (Message<String> m) ->
                        m.reply(nearest.get(
                                stream(m.body().split(","))
                                        .mapToDouble(Double::parseDouble)
                                        .toArray())));
    }

    private void get(String file, Consumer<Stream<String>> setter) {
        String filename = file + ".csv";

        String uri = "/api2/FileService" +
                "?key=" + container.config().getString("FileService") +
                "&filename=" + filename;

        Handler<Buffer> handler = buf -> {
            System.out.println("got " + filename);
            String s = buf.toString();

            if (s.length() < 1000) {
                System.err.println(s);
            }

            setter.accept(new BufferedReader(new StringReader(s)).lines());
        };

        vertx.createHttpClient()
                .setHost("api.sl.se")
                .get(uri, rsp -> rsp.bodyHandler(handler))
                .putHeader("Accept", "application/json")
                .end();
    }
}
