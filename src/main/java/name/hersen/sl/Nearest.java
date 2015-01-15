package name.hersen.sl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Nearest extends Verticle {

    private NearestImpl nearest;
    static Logger l;

    public void start() {
        l = container.logger();
        nearest = new NearestImpl();

        get("Sites", nearest::setSites);
        get("StopPoints", nearest::setStopPoints);

        vertx.eventBus().registerHandler("nearest", (Message<JsonObject> m) -> m.reply(nearest.get(m.body())));
    }

    private void get(String file, Consumer<Stream<String>> setter) {
        String filename = file + ".csv";

        String uri = "/api2/FileService" +
                "?key=" + container.config().getString("FileService") +
                "&filename=" + filename;

        Handler<Buffer> handler = buf -> {
            Logger l = container.logger();
            l.info("got " + filename);
            String s = buf.toString();

            if (s.length() < 1000) {
                l.error(s);
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
