package garden.bots.engines;

import garden.bots.data.Data;
import garden.bots.resources.FunctionPayload;
import io.vavr.Function1;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
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
    EngineLogger log = EngineLogger.start("compilation");

    //return Try.of(() -> engine.eval(funktion.code));

    Try<Object> compilation = Try.of(() -> engine.eval(funktion.code));

    if(compilation.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 JavaScript Error] " + compilation.getCause().getMessage()));
    } else {
      log.end(new EngineEvent("success",funktion, ""));
    }

    return compilation;
  };

  public static Single<JsonObject> compile(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<Object, Single<JsonObject>> success) {
    EngineLogger log = EngineLogger.start("compilation");

    Try<Object> compilation = Try.of(() -> engine.eval(funktion.code));

    if(compilation.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 JavaScript Error] " + compilation.getCause().getMessage()));

      return failure.apply(compilation.getCause());
    } else {
      log.end(new EngineEvent("success",funktion, ""));

      return success.apply(compilation.get());
    }
  }


  public static Try<ScriptObjectMirror> execute(FunctionPayload funktion) {

    EngineLogger log = EngineLogger.start("execution");

    Try<ScriptObjectMirror> execution =  Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name, funktion.parameters));

    if(execution.isFailure()) {
      log.end(new EngineEvent("failure",funktion, "[😡 JavaScript Error] " + execution.getCause().getMessage()));

    } else {
      log.end(new EngineEvent("success",funktion, execution.get().getMember("result")));
    }

    return execution;
  }

  public static Single<JsonObject> execute(FunctionPayload funktion, Function1<Throwable, Single<JsonObject>> failure, Function1<ScriptObjectMirror, Single<JsonObject>> success) {

    EngineLogger log = EngineLogger.start("execution");

    Try<ScriptObjectMirror> execution;

    if(funktion.parameters.size() == 0) {
      log.send("Execution of "+funktion.name);

      execution = Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name)); // use version

    } else {
      log.send("Execution of "+funktion.name+ " with parameters: " + funktion.parameters);

      execution = Try.of(() -> (ScriptObjectMirror) inv.invokeFunction(funktion.name, funktion.parameters));
    }

    if(execution.isFailure()) {

      log.end(new EngineEvent("failure",funktion, "[😡 JavaScript Error] " + execution.getCause().getMessage()));

      return failure.apply(execution.getCause());
    } else {

      log.end(new EngineEvent("success",funktion, execution.get().getMember("result")));

      return success.apply(execution.get());
    }
  }

}
