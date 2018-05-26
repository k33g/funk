package garden.bots.resources;


import garden.bots.data.Data;
import garden.bots.engines.JSEngine;
import garden.bots.singles.SingleJson;
import garden.bots.token.Check;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.servicediscovery.Record;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import rx.Single;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;


@Path("/funk/js")
public class JSFunctionsResource {

  @Produces("application/json; charset=utf-8")
  @Path("/run")
  @POST
  public Single<JsonObject> run(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken, JsonObject data) {

    //Funk.vertx(vertx);

    data.put("kind", "js");
    FunctionPayload funktion = FunctionPayload.of(data);

    Function1<Record, Boolean> filterFunction = record ->
      record.getName().equals(funktion.name) && record.getMetadata().getString("kind").equals(funktion.kind);

    Function1<Throwable, Single<JsonObject>> newExecutionKO = (error) -> SingleJson.error(error.getCause().getMessage());

    Function1<ScriptObjectMirror, Single<JsonObject>> newExecutionOK = (executionResult) -> SingleJson.result(executionResult.getMember("result"));

    Function1<Throwable, Single<JsonObject>> executionKO = (error) -> {
      /* ----- function does not exist in memory ----- */
      System.out.println("==============================================");
      System.out.println(" Execution error of " + funktion.name + "(js)");
      System.out.println(" Trying to evaluate again the function...");
      System.out.println("==============================================");

      /* ----- search the function ----- */

      return Data.searchFunction(vertx, filterFunction,
        () -> SingleJson.error("the function does not exists"),
        /* ----- the function exists in the backen but not in memory, so we need to compile it ----- */
        /* ----- compilation ----- */
        record -> JSEngine.compile(
          FunctionPayload.from(record),
          compilationError -> SingleJson.error(compilationError.getCause().getMessage()),
          /* ----- execution [again] ----- */
          compilationSuccess -> JSEngine.execute(funktion, newExecutionKO, newExecutionOK)
        )
      );
    };

    Function1<ScriptObjectMirror, Single<JsonObject>> executionOK = (executionResult) -> SingleJson.result(executionResult.getMember("result"));

    Function0<Single<JsonObject>> tokenKO = () -> SingleJson.error("Bad token");

    Function0<Single<JsonObject>> tokenOK = () -> JSEngine.execute(funktion, executionKO, executionOK);

    return Check.token(funkToken, tokenKO, tokenOK);

  }


  @Produces("application/json; charset=utf-8")
  //@Path("/list")
  @GET
  public Single<JsonObject> functions(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken) {

    Function1<Record, Boolean> filterAllFunctions = record -> record.getMetadata().getString("kind").equals("js");

    return  Check.token(funkToken).isSuccess()
      ? Data.functionsList(vertx, filterAllFunctions)
      : SingleJson.error("Bad token");

  }

  @Produces("application/json; charset=utf-8")
  //@Path("/create")
  @POST
  public Single<JsonObject> create(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken, JsonObject data) {
    data.put("kind", "js");
    FunctionPayload funktion = FunctionPayload.of(data);
    Record recordFunction = funktion.getRecord();

    Function1<Record, Boolean> filterFunction =
      record ->
        record.getName().equals(funktion.name) &&
          record.getMetadata().getString("kind").equals(funktion.kind);


    System.out.println("==============================================");
    System.out.println(" Create " + funktion.name + "("+funktion.kind+")");
    System.out.println("==============================================");

    /* ----- search the function ----- */
    return Data.searchFunction(vertx, filterFunction,
      /* ----- the function does not exist => create ----- */
      () -> JSEngine.compile( /* ----- compilation ----- */
        funktion,
        compilationError -> SingleJson.error(compilationError.getMessage()),
        compilationSuccess -> Check.token(funkToken).isSuccess()
          ? Data.createFunction(vertx, recordFunction)
          : SingleJson.error("Bad token")
      ),
      /* ----- the function already exists ----- */
      record -> Check.token(funkToken).isSuccess()
        ? SingleJson.error("This function already exists, you must use update")
        : SingleJson.error("Bad token")
    );
  }

  @Produces("application/json; charset=utf-8")
  //@Path("/update")
  @PUT
  public Single<JsonObject> update(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken, JsonObject data) {
    data.put("kind", "js");
    FunctionPayload funktion = FunctionPayload.of(data);

    Function1<Record, Boolean> filterFunction =
      record ->
        record.getName().equals(funktion.name) &&
          record.getMetadata().getString("kind").equals(funktion.kind);

    System.out.println("==============================================");
    System.out.println(" Update " + funktion.name + "("+funktion.kind+")");
    System.out.println("==============================================");

    return Check.token(
      funkToken,
      () -> SingleJson.error("Bad token"),
      () -> Data.searchFunction(vertx, filterFunction,
        /* ----- the function does not exist ----- */
        () -> SingleJson.error("This function does not exist, you must use create"),
        /* ----- the function already exists ----- */
        recordFunction -> {
          /* ----- update record ----- */
          Record record = Data.updateRecord(recordFunction, funktion);
          /* ----- compilation ----- */
          return JSEngine.compile(
            funktion,
            compilationError -> SingleJson.error(compilationError.getMessage()),
            compilationSuccess -> {
              /* ----- notify the other instances ----- */
              JsonObject message = new JsonObject()
                .put("what", "update")
                .put("name", funktion.name)
                .put("kind", funktion.kind)
                .put("code", funktion.code)
                .put("dependencies", funktion.dependencies)
                .put("sender", Data.instanceName());

              Data.redis(vertx).publish("changes", message.encode(),res -> {
                //TODO if (res.succeeded()) {}
              });
              return Data.updateFunction(vertx, record);
            }
          );
        }
      )
    );
  }

}
