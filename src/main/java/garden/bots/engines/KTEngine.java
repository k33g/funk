package garden.bots.engines;

import io.vavr.Function1;
import io.vavr.control.Try;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import rx.Single;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

public class KTEngine {

  private static ScriptEngineManager engineManager = new ScriptEngineManager();
  private static ScriptEngine engine = engineManager.getEngineByExtension("kts");
  private static Invocable inv = (Invocable) engine;

  public static Try<Object> compile(String sourceCode, JsonArray dependencies) {
      return Try.of(() -> engine.eval(sourceCode));
  };

  public static Single<JsonObject> compile(String sourceCode, JsonArray dependencies, Function1<Throwable, Single<JsonObject>> failure, Function1<Object, Single<JsonObject>> success) {
    Try<Object> compilation = Try.of(() -> engine.eval(sourceCode));

    if(compilation.isFailure()) {
      return failure.apply(compilation.getCause());
    } else {
      return success.apply(compilation.get());
    }
  }

  public static Try<JsonObject> execute(String functionName, Map<String, Object> parameters) {
    return Try.of(() -> (JsonObject) inv.invokeFunction(functionName, parameters));
  }

  public static Single<JsonObject> execute(String functionName, Map<String, Object> parameters, Function1<Throwable, Single<JsonObject>> failure, Function1<JsonObject, Single<JsonObject>> success) {

    Try<JsonObject> execution;

    if(parameters.size() == 0) {
      System.out.println("=========== Execution without parameter ===========");
      System.out.println(" - parameters: " + parameters);
      System.out.println(" - size: " + parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(functionName));

    } else {
      System.out.println("=========== Execution with parameters =============");
      System.out.println(" - parameters: " + parameters);
      System.out.println(" - size: " + parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (JsonObject) inv.invokeFunction(functionName, parameters));
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
