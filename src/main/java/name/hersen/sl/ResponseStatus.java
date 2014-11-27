package name.hersen.sl;

import org.vertx.java.core.json.JsonObject;

public class ResponseStatus {
    public final boolean isOk;
    public final boolean isThrottled;

    public ResponseStatus(boolean isOk, boolean isThrottled) {
        this.isOk = isOk;
        this.isThrottled = isThrottled;
    }

    public static ResponseStatus valueOf(JsonObject jsonObject) {
        Integer statusCode = jsonObject.getInteger("StatusCode");
        if (statusCode == null) {
            return new ResponseStatus(true, false);
        }
        return new ResponseStatus(statusCode == 0, statusCode == 1006);
    }
}
