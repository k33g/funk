package garden.bots.singles;

import io.vertx.core.json.JsonObject;
import rx.Single;

public class SingleJson {
    public static Single<JsonObject> error(String message) {
        return Single.just(new JsonObject().put("error", message));
    }

    public static Single<JsonObject> result(Object result) {
        return Single.just(new JsonObject().put("result", result));
    }
}
