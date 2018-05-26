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
      return Try.of(() -> engine.eval(funktion.code));
  };

  public static Single<JsonObject> compile(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<Object, Single<JsonObject>> success) {
    Try<Object> compilation = Try.of(() -> engine.eval(funktion.code));
    if(compilation.isFailure()) {
      return failure.apply(compilation.getCause());
    } else {
      return success.apply(compilation.get());
    }
  }

  public static Try<JsonObject> execute(FunctionPayload funktion) {
    return Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name, funktion.parameters));
  }

  public static Single<JsonObject> execute(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<JsonObject, Single<JsonObject>> success) {

    Try<JsonObject> execution;

    if(funktion.parameters.size() == 0) {
      System.out.println("=========== Execution without parameter ===========");
      System.out.println(" - parameters: " + funktion.parameters);
      System.out.println(" - size: " + funktion.parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name));

    } else {
      System.out.println("=========== Execution with parameters =============");
      System.out.println(" - parameters: " + funktion.parameters);
      System.out.println(" - size: " + funktion.parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(funktion.name, funktion.parameters));
    }

    if(execution.isFailure()) {
      System.out.println("============[😡 Kotlin Error]======================");
      System.out.println(execution.getCause().getMessage());
      System.out.println("===================================================");
        return failure.apply(execution.getCause());
    } else {
        return success.apply(execution.get());
    }
  }
}
