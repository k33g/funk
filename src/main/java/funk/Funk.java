package funk;

import garden.bots.data.Data;
import garden.bots.engines.JSEngine;
import garden.bots.engines.KTEngine;
import garden.bots.resources.FunctionPayload;
import io.vavr.Function1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class Funk {

  //private static Vertx vertx;

  private String name;
  //private WebClient client;
  private String kind;
  private JsonObject parameters;

  /*
  public static void vertx(Vertx vertx) {
    if(Funk.vertx == null) {
      Funk.vertx = vertx;
    }
  }
  */

  public Funk(String name) {
    this.name = name;
    //this.client =  WebClient.create(Funk.vertx);
  }

  public Funk(String name, String kind) {
    this.name = name;
    this.kind = kind;
    //this.client =  WebClient.create(Funk.vertx);
  }

  public static Funk name(String name) {
    return new Funk(name);
  }

  public static Funk name(String name, String kind) {
    return new Funk(name, kind);
  }

  public static Funk kt(String name) {
    return new Funk(name, "kt");
  }
  public static Funk js(String name) {
    return new Funk(name, "js");
  }

  public Funk kind(String kind) {
    this.kind = kind;
    return this;
  }

  public JsonObject call() {
    switch (this.kind) {
      case "js":
        return this.execJS(null);

      case "kt":
        return this.execKT(null);

      default:
        System.out.println(String.format("%s extension is not managed by Funk", kind));

    };
    return null;
  }

  public JsonObject call(JsonObject parameters) {
    switch (this.kind) {
      case "js":
        return this.execJS(parameters);

      case "kt":
        return this.execKT(parameters);

      default:
        System.out.println(String.format("%s extension is not managed by Funk", kind));
    }
    return null;
  }

  private JsonObject execJS(JsonObject parameters) {
    //System.out.println("🎃 " + this.name + " 👋 " + this.kind);
    //System.out.println("parameters: " + parameters);
    this.parameters = parameters;

    Function1<Throwable, JsonObject> executionKO = (error) -> {
      /* ----- function does not exist in memory ----- */
      /* ----- search the function ----- */
      Option<JsonObject> optionalFunktion = Data.syncSearchFunctiont(this.name, this.kind);

      System.out.println(optionalFunktion.get().getString("name"));
      System.out.println(optionalFunktion.get().getString("name"));

      if(optionalFunktion.isEmpty()) {
        return new JsonObject().put("error", "the function does not exists");

      } else {

        FunctionPayload funktion = FunctionPayload.from(this.name, optionalFunktion.get().getJsonObject("metadata").getString("code"));

        Try<Object> compilation = JSEngine.compile(funktion);
        if(compilation.isFailure()) {
          return new JsonObject().put("error", compilation.getCause().getMessage());
        } else {
          // compilation ok \o/ => run it
          Try<ScriptObjectMirror> execution = JSEngine.execute(funktion);

          if(execution.isFailure()) {
            return new JsonObject().put("error", execution.getCause().getMessage());
          } else {
            return new JsonObject().put("result", execution.get().getMember("result"));
          }
        }
      }
    };

    Function1<ScriptObjectMirror, JsonObject> executionOK = (executionResult) -> new JsonObject().put("result", executionResult.getMember("result"));

    Try<ScriptObjectMirror> execution = JSEngine.execute(FunctionPayload.from(this.name, null,this.kind, this.parameters));

    return execution.isFailure()
      ? executionKO.apply(execution.getCause())
      : executionOK.apply(execution.get());

  }

  private JsonObject execKT(JsonObject parameters) {
    //System.out.println("🎃 " + this.name + " 👋 " + this.kind);
    //System.out.println("parameters: " + parameters);
    this.parameters = parameters;

    Function1<Throwable, JsonObject> executionKO = (error) -> {
      /* ----- function does not exist in memory ----- */
      /* ----- search the function ----- */
      Option<JsonObject> optionalFunktion = Data.syncSearchFunctiont(this.name, this.kind);

      System.out.println(optionalFunktion.get().getString("name"));
      System.out.println(optionalFunktion.get().getString("name"));

      if(optionalFunktion.isEmpty()) {
        return new JsonObject().put("error", "the function does not exists");

      } else {

        FunctionPayload funktion = FunctionPayload.from(this.name, optionalFunktion.get().getJsonObject("metadata").getString("code"));

        Try<Object> compilation = KTEngine.compile(funktion);
        if(compilation.isFailure()) {
          return new JsonObject().put("error", compilation.getCause().getMessage());
        } else {
          // compilation ok \o/ => run it
          Try<JsonObject> execution = KTEngine.execute(funktion);

          if(execution.isFailure()) {
            return new JsonObject().put("error", execution.getCause().getMessage());
          } else {
            return new JsonObject().put("result", execution.get().getValue("result"));
          }
        }
      }
    };

    Function1<ScriptObjectMirror, JsonObject> executionOK = (executionResult) -> new JsonObject().put("result", executionResult.getMember("result"));

    Try<ScriptObjectMirror> execution = JSEngine.execute(FunctionPayload.from(this.name, null,this.kind, this.parameters));

    return execution.isFailure()
      ? executionKO.apply(execution.getCause())
      : executionOK.apply(execution.get());

  }

}


