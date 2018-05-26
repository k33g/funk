package garden.bots.engines;

import garden.bots.resources.FunctionPayload;
import io.vavr.Function1;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import rx.Single;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class JSEngine {
  //private static ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine("--language=es6");
  private static ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();

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


  public static Try<ScriptObjectMirror> execute(FunctionPayload funktion) {
    return Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name, funktion.parameters));
  }

  public static Single<JsonObject> execute(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<ScriptObjectMirror, Single<JsonObject>> success) {

    Try<ScriptObjectMirror> execution;

    if(funktion.parameters.size() == 0) {
      System.out.println("=========== Execution without parameter ===========");
      System.out.println(" - parameters: " + funktion.parameters);
      System.out.println(" - size: " + funktion.parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name)); // use version

    } else {
      System.out.println("=========== Execution with parameters =============");
      System.out.println(" - parameters: " + funktion.parameters);
      System.out.println(" - size: " + funktion.parameters.size());
      System.out.println("===================================================");

      execution = Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name, funktion.parameters));
    }

    if(execution.isFailure()) {
      System.out.println("============[😡 JavaScript Error]==================");
      System.out.println(execution.getCause().getMessage());
      System.out.println("===================================================");
      return failure.apply(execution.getCause());
    } else {
      return success.apply(execution.get());
    }
  }

}
