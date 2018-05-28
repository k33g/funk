package garden.bots;

import garden.bots.data.Data;
import garden.bots.engines.JSEngine;
import garden.bots.engines.KTEngine;
import garden.bots.resources.FunctionPayload;
import garden.bots.security.FunkSecurityManager;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import me.atrox.haikunator.Haikunator;
import me.atrox.haikunator.HaikunatorBuilder;
import net.redpipe.engine.core.Server;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Main {
  public static void main(String[] args) throws ClassNotFoundException {
    JsonObject config = new JsonObject();
    Integer httport = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8080"));
    config.put("http_port", httport);

    Vertx vertx = io.vertx.rxjava.core.Vertx.vertx();

    //System.setSecurityManager(new FunkSecurityManager());


    //https://vertx.io/docs/vertx-redis-client/java/#_redis_sentinel
    vertx.eventBus().<JsonObject>consumer("io.vertx.redis.changes", received -> {
      JsonObject value = received.body().getJsonObject("value");
      System.out.println("==========================================================================================");

      JsonObject message = new JsonObject(value.getString("message"));

      String sender = message.getString("sender");
      String what = message.getString("what");
      String kind = message.getString("kind");
      String name = message.getString("name");
      String code = message.getString("code");

      //JsonArray dependencies = message.getJsonArray("dependencies");

      System.out.println("sender: " + sender);
      System.out.println("what: " + what);
      System.out.println("kind: " + kind);
      System.out.println("name: " + name);
      System.out.println("code: " + code);

      if(Data.instanceName().equals(sender)) {
        System.out.println("-> sender and subscriber are the same instance, no need to recompile");
      } else {
        System.out.println(String.format("-> new compilation of %s by %s", name, Data.instanceName()));
        //TODO manage compilation errors
        switch (kind) {
          case "js":
            JSEngine.compile(FunctionPayload.from(name, code));
            break;

          case "kt":
            KTEngine.compile(FunctionPayload.from(name, code));
            break;

          default:
            System.out.println(String.format("%s extension is not managed by Funk", kind));
        }
      }

      System.out.println("==========================================================================================");
    });

    vertx.eventBus().<JsonObject>consumer("io.vertx.redis.logs", received -> {
      JsonObject value = received.body().getJsonObject("value");
      System.out.println(value);
    });

    vertx.eventBus().<JsonObject>consumer("io.vertx.redis.monitor", received -> {
      JsonObject value = received.body().getJsonObject("value");
      System.out.println(value);

    });

    Data.redis(vertx).subscribe("monitor", res -> {
      if (res.succeeded()) {
        // TODO: use some colors
        System.out.println("==========================================================================================");
        System.out.println(" Registration to #monitor channel is ok");
        System.out.println("==========================================================================================");
      }
    });

    Data.redis(vertx).subscribe("logs", res -> {
      if (res.succeeded()) {
        // TODO: use some colors
        System.out.println("==========================================================================================");
        System.out.println(" Registration to #logs channel is ok");
        System.out.println("==========================================================================================");
      }
    });

    Data.redis(vertx).subscribe("changes", res -> {
      if (res.succeeded()) {
        // TODO: use some colors
        System.out.println("==========================================================================================");
        System.out.println(" Registration to #changes channel is ok");
        System.out.println("==========================================================================================");
      }
    });

    Haikunator haikunator = new HaikunatorBuilder().setTokenLength(6).build();
    Data.instanceName(haikunator.haikunate() + "-" + UUID.randomUUID());

    //Data.getSyncRedisClient().append(Data.instanceName(), new Date().toString());

    new Server().start(config, Resources.getList())
      .subscribe(
              v -> {
                System.out.println("RedPipe server is started");

                //SecurityManager securityManager = new SecurityManager();
                SecurityManager securityManager = new FunkSecurityManager();
                System.setSecurityManager(securityManager);


                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                  System.out.println("===== SECURITY MANAGER =====");
                  //security.checkExit(status);
                } else {
                  System.out.println("===== NO SECURITY MANAGER =====");
                }


                Data.redis(vertx).publish("logs", Data.instanceName() + " started...",res -> {
                  //TODO if (res.succeeded()) {}
                });
              },
              Throwable::printStackTrace
      );
  }
}
