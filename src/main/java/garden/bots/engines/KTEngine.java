package garden.bots.engines;

import garden.bots.resources.FunctionPayload;
import io.vavr.Function1;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import rx.Single;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class KTEngine {

  private static ScriptEngineManager engineManager = new ScriptEngineManager();
  private static ScriptEngine engine = engineManager.getEngineByExtension("kts");
  private static Invocable inv = (Invocable) engine;

  public static Try<Object> compile(FunctionPayload funktion) {
    EngineLogger log = EngineLogger.start("compilation");

    Try<Object> compilation = Try.of(() -> engine.eval(funktion.code));

    if(compilation.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 Kotlin Error] " + compilation.getCause().getMessage()));
    } else {
      log.end(new EngineEvent("success",funktion, ""));
    }

    return compilation;
  };

  public static Single<JsonObject> compile(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<Object, Single<JsonObject>> success) {
    EngineLogger log = EngineLogger.start("compilation");

    Try<Object> compilation = Try.of(() -> engine.eval(funktion.code));
    if(compilation.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 Kotlin Error] " + compilation.getCause().getMessage()));

      return failure.apply(compilation.getCause());
    } else {
      log.end(new EngineEvent("success",funktion, ""));

      return success.apply(compilation.get());
    }
  }

  /*
  public static Try<JsonObject> execute(FunctionPayload funktion) {
    return Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name, funktion.parameters));
  }
  */

  public static Try<JsonObject> execute(FunctionPayload funktion) {

    EngineLogger log = EngineLogger.start("execution");

    Try<JsonObject> execution =  Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name, funktion.parameters));

    if(execution.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 Kotlin Error] " + execution.getCause().getMessage()));

    } else {
      log.end(new EngineEvent("success",funktion, execution.get().getValue("result")));
    }

    return execution;
  }





  public static Single<JsonObject> execute(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<JsonObject, Single<JsonObject>> success) {

    EngineLogger log = EngineLogger.start("execution");

    Try<JsonObject> execution;

    if(funktion.parameters.size() == 0) {
      log.send("Execution of "+funktion.name);

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name));

    } else {
      log.send("Execution of "+funktion.name+ " with parameters: " + funktion.parameters);

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name, funktion.parameters));
    }

    if(execution.isFailure()) {

      log.end(new EngineEvent("failure",funktion, "[😡 Kotlin Error] " + execution.getCause().getMessage()));

      return failure.apply(execution.getCause());
    } else {
      log.end(new EngineEvent("success",funktion, execution.get().getValue("result")));

      return success.apply(execution.get());
    }
  }
}
