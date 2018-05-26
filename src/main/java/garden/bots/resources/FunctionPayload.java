package garden.bots.resources;

import garden.bots.data.Data;
import io.vavr.control.Option;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import java.util.Map;

public class FunctionPayload {

  public String name;
  public Map<String, Object> parameters;
  public String description;
  public String code;
  public String kind;
  public JsonArray dependencies;


  public FunctionPayload(JsonObject payload) {

    /* ----- function name ----- */
    Option<String> optionalFunctionName = Option.of(payload.getString("name"));
    name = optionalFunctionName.getOrElse("unknown");


    /* ----- parameters ----- */
    Option<JsonObject> optionalParameters = Option.of(payload.getJsonObject("parameters"));
    parameters = optionalParameters.getOrElse(new JsonObject()).getMap();


    /* ----- dependencies ----- */
    Option<JsonArray> optionalDependencies = Option.of(payload.getJsonArray("dependencies"));
    dependencies = optionalDependencies.getOrElse(new JsonArray());

    /* ----- function code ----- */
    Option<String> optionalCode = Option.of(payload.getString("code"));
    code = optionalCode.getOrElse("unknown");

    /* ----- function code ----- */
    Option<String> optionalKind = Option.of(payload.getString("kind"));
    kind = optionalKind.getOrElse("unknown");

  }

  public Record getRecord() {
    return Data.createFunctionRecord(
      name, description, code, kind, dependencies
    );
  }

  public static FunctionPayload of(JsonObject payload) {
    return new FunctionPayload(payload);
  }

  public static FunctionPayload from(Record existingRecord) {
    return new FunctionPayload(new JsonObject()
      .put("name", existingRecord.getName())
      .put("code", existingRecord.getMetadata().getString("code"))
      .put("description", existingRecord.getMetadata().getString("description"))
      .put("kind", existingRecord.getMetadata().getString("kind"))
      .put("dependencies", existingRecord.getMetadata().getJsonArray("dependencies"))
    );
  }

  public static FunctionPayload from(String name, String code) {
    return new FunctionPayload(new JsonObject()
      .put("name", name)
      .put("code", code)
    );
  }

  public static FunctionPayload from(String name, String code, String kind) {
    return new FunctionPayload(new JsonObject()
      .put("name", name)
      .put("code", code)
      .put("kind", kind)
    );
  }
}


