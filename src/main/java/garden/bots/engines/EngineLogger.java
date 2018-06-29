package garden.bots.engines;

import garden.bots.data.Data;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;

import java.util.Date;

public class EngineLogger {

  //private static Vertx vertx = io.vertx.rxjava.core.Vertx.vertx();
  private static Vertx vertx = null;

  public static void vertx(Vertx vertx) {
    if(EngineLogger.vertx == null) {
      EngineLogger.vertx = vertx;
    }
  }

  private long startTime;
  private long elapsedTime;
  private String action;
  private Date when;

  public EngineLogger(String action) {
    this.startTime = System.nanoTime();
    this.action = action;
    this.when = new Date();
  }

  public EngineLogger send(String message) {
    Data.redis(vertx).publish("logs", message, res -> {
      //TODO if (res.succeeded()) {}
    });
    return this;
  }

  public EngineLogger end(EngineEvent event) {
    this.elapsedTime = (System.nanoTime() - this.startTime)/1000000;
    event.action = this.action;
    event.elapsedTime = this.elapsedTime;
    event.startTime = this.elapsedTime;
    event.when = this.when;

    //event.funktion = FunctionPayload.from(event.funktion.name,null, event.funktion.kind, JsonObject.mapFrom(event.funktion.parameters));

    Data.redis(vertx).publish("monitor", JsonObject.mapFrom(event).encode(), res -> {
      //TODO if (res.succeeded()) {}
    });

    return this;
  }

  public static EngineLogger start(String action) {
    return new EngineLogger(action);
  }
}
