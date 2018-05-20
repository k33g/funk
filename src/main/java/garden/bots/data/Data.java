package garden.bots.data;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.control.Option;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisOptions;
import io.vertx.rxjava.core.Vertx;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.redis.RedisClient;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import me.atrox.haikunator.Haikunator;
import me.atrox.haikunator.HaikunatorBuilder;
import rx.Single;


import java.util.Optional;
import java.util.UUID;

public class Data {

    private static Integer redisPort = Integer.parseInt(Optional.ofNullable(System.getenv("REDIS_PORT")).orElse("6379"));
    private static String redisHost = Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("127.0.0.1");
    private static String redisAuth = Optional.ofNullable(System.getenv("REDIS_PASSWORD")).orElse(null);
    //private static String redisRecordsKey = Optional.ofNullable(System.getenv("REDIS_RECORDS_KEY")).orElse("funk");    // the redis hash

    /* ===== common functions values ===== */
    private static String serviceHost = Optional.ofNullable(System.getenv("SERVICE_HOST")).orElse("localhost"); // domain name
    private static Integer servicePort = Integer.parseInt(Optional.ofNullable(System.getenv("SERVICE_PORT")).orElse("8080")); // set to 80 on Clever Cloud
    private static String serviceRoot = Optional.ofNullable(System.getenv("SERVICE_ROOT")).orElse("/funk");

    private static RedisClient redis = null;

    public static RedisClient redis(Vertx vertx) {
        if(redis==null) {
            RedisOptions config = new RedisOptions()
                    .setHost(redisHost)
                    .setPort(redisPort)
                    .setAuth(redisAuth);

            redis = RedisClient.create(vertx, config);
        }
        return redis;
    }

    private static ServiceDiscovery discovery = null;

    public static ServiceDiscovery discovery(Vertx vertx) {

        if(discovery==null) {
            ServiceDiscoveryOptions serviceDiscoveryOptions = new ServiceDiscoveryOptions();
            JsonObject config = new JsonObject();
            discovery = ServiceDiscovery.create(vertx, serviceDiscoveryOptions.setBackendConfiguration(config));
        }

        return discovery;
    };

    private static String instanceName = null;

    public static void instanceName(String name) {
        instanceName = name;
    }

    public static String instanceName() {
        if(instanceName==null) {
            Haikunator haikunator = new HaikunatorBuilder().setTokenLength(6).build();
            instanceName = haikunator.haikunate() + "-" + UUID.randomUUID();
        }
        return instanceName;
    }


    public static Record createFunctionRecord(String functionName, String description, String code, String kind) {
        // create the microservice/function record
        Record record = HttpEndpoint.createRecord(
                functionName,
                serviceHost,
                servicePort,
                serviceRoot
        );

        // add some metadata
        record.setMetadata(new JsonObject()
                .put("code", code)
                .put("description", description)
                .put("kind", kind)
        );

        return record;
    };

    public static Single<JsonObject> searchFunction(Vertx vertx, Function1<Record, Boolean> filterFunction, Function0<Single<JsonObject>> none, Function1<Record, Single<JsonObject>> some) {
        return Data.discovery(vertx).rxGetRecord(filterFunction).map(record -> Option.of(record)).flatMap(record -> {
            return record.isEmpty() ? none.apply() : some.apply(record.get());
        });

    }
    public static Single<JsonObject> functionsList(Vertx vertx, Function1<Record, Boolean> filterAllFunctions) {
        return Data.discovery(vertx).rxGetRecords(filterAllFunctions).map(records -> new JsonObject().put("functions",new JsonArray(records)));
    }

    public static Single<JsonObject> updateFunction(Vertx vertx, Record recordFunction) {

        return Data.discovery(vertx).rxUpdate(recordFunction).map(publishedRecord -> publishedRecord.toJson());
    }

    public static Single<JsonObject> createFunction(Vertx vertx, Record recordFunction) {

        return Data.discovery(vertx).rxPublish(recordFunction).map(publishedRecord -> publishedRecord.toJson());
    }

    public static Record updateRecord(Record record, String description, String code) {
        record.getMetadata().put("description", description).put("code", code);
        return record;
    }
}
