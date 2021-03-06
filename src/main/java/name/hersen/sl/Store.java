package name.hersen.sl;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import static java.util.Arrays.asList;

public class Store extends Verticle {

    private StoreImpl store;

    public void start() {
        store = new StoreImpl();
        store.putStations("northwest", asList(9710, 9711, 9700, 9701, 9702, 9703, 9704, 9325));
        store.putStations("northeast", asList(9502, 9503, 9504, 9505, 9506, 9507, 9508, 9509));
        store.putStations("central", asList(9510, 9000, 9530, 9531, 9529));
        store.putStations("southwest", asList(9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520, 9543));
        store.putStations("southeast", asList(9180, 9732, 9731, 9730, 9729, 9728, 9727, 9726, 9725, 9724));

        vertx.eventBus().registerHandler("store.put", (Message<JsonObject> message) -> store.put(message.body()));
        vertx.eventBus().registerHandler("store.stations", (Message m) -> m.reply(store.get()));
    }
}
