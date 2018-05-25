package garden.bots.resources;


import garden.bots.data.Data;
import garden.bots.engines.KTEngine;
import garden.bots.singles.SingleJson;
import garden.bots.token.Check;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.control.Option;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.servicediscovery.Record;
import rx.Single;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Map;


@Path("/funk/kt")
public class KTFunctionsResource {


  @Produces("application/json; charset=utf-8")
  @Path("/run")
  @POST
  public Single<JsonObject> run(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken, JsonObject data) {
    //TODO: manage errors
    String functionName = data.getString("name");

    /* ----- if no parameters ----- */
    Option<JsonObject> optionalParameters = Option.of(data.getJsonObject("parameters"));
    Map parameters = optionalParameters.getOrElse(new JsonObject()).getMap();

    Function1<Record, Boolean> filterFunction = record -> record.getName().equals(data.getString("name")) && record.getMetadata().getString("kind").equals("kt");

    Function1<Throwable, Single<JsonObject>> newExecutionKO = (error) -> SingleJson.error(error.getCause().getMessage());

    Function1<JsonObject, Single<JsonObject>> newExecutionOK = (executionResult) -> SingleJson.result(executionResult.getValue("result"));

    Function1<Throwable, Single<JsonObject>> executionKO = (error) -> {
      /* ----- function does not exist in memory ----- */
      System.out.println("==============================================");
      System.out.println(" Execution error of " + functionName + "(kt)");
      System.out.println(" Trying to evaluate again the function...");
      System.out.println("==============================================");

      /* ----- search the function ----- */

      return Data.searchFunction(vertx, filterFunction,
        () -> SingleJson.error("the function does not exists"),
        /* ----- the function exists in the backend but not in memory, so we need to compile it ----- */
        /* ----- compilation ----- */
        record -> KTEngine.compile(
          record.getMetadata().getString("code"), record.getMetadata().getJsonArray(" dependencies"),
          compilationError -> SingleJson.error(compilationError.getCause().getMessage()),
          /* ----- execution [again] ----- */
          compilationSuccess -> KTEngine.execute(functionName, parameters, newExecutionKO, newExecutionOK)
        )
      );

    };

    Function1<JsonObject, Single<JsonObject>> executionOK = (executionResult) -> SingleJson.result(executionResult.getValue("result"));

    Function0<Single<JsonObject>> tokenKO = () -> SingleJson.error("Bad token");

    Function0<Single<JsonObject>> tokenOK = () -> KTEngine.execute(functionName, parameters, executionKO, executionOK);

    return Check.token(funkToken, tokenKO, tokenOK);

  }


  @Produces("application/json; charset=utf-8")
  //@Path("/list")
  @GET
  public Single<JsonObject> functions(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken) {

    Function1<Record, Boolean> filterAllFunctions = record -> record.getMetadata().getString("kind").equals("kt");

    return  Check.token(funkToken).isSuccess()
      ? Data.functionsList(vertx, filterAllFunctions)
      : SingleJson.error("Bad token");

  }

  @Produces("application/json; charset=utf-8")
  //@Path("/create")
  @POST
  public Single<JsonObject> create(@Context Vertx vertx, @HeaderParam("funk-token") String funkToken, JsonObject data) {

    Record recordFunction = Data.createFunctionRecord(
      data.getString("name"),
      data.getString("description"),
      data.getString("code"),
      "kt"
    );

    /* ----- if no dependencies ----- */
    Option<JsonArray> optionalDependencies = Option.of(data.getJsonArray("dependencies"));
    JsonArray dependencies = optionalDependencies.getOrElse(new JsonArray());

    recordFunction.getMetadata().put("dependencies",  dependencies);

    String sourceCode = data.getString("code");

    Function1<Record, Boolean> filterFunction =
      record ->
        record.getName().equals(data.getString("name")) &&
          record.getMetadata().getString("kind").equals("kt");

    /* ----- search the function ----- */
    return Data.searchFunction(vertx, filterFunction,
      /* ----- the function does not exist => create ----- */
      () -> KTEngine.compile( /* ----- compilation ----- */
        sourceCode, dependencies,
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

    String sourceCode = data.getString("code");
    String description = data.getString("description");
    String functionName = data.getString("name");

    /* ----- if no dependencies ----- */
    Option<JsonArray> optionalDependencies = Option.of(data.getJsonArray("dependencies"));
    JsonArray dependencies = optionalDependencies.getOrElse(new JsonArray());

    Function1<Record, Boolean> filterFunction = record -> record.getName().equals(functionName) && record.getMetadata().getString("kind").equals("kt");

    return Check.token(
      funkToken,
      () -> SingleJson.error("Bad token"),
      () -> Data.searchFunction(vertx, filterFunction,
        /* ----- the function does not exist ----- */
        () -> SingleJson.error("This function does not exist, you must use create"),
        /* ----- the function already exists ----- */
        recordFunction -> {
          /* ----- update record ----- */
          Record record = Data.updateRecord(recordFunction, description, sourceCode, dependencies);
          /* ----- compilation ----- */
          return KTEngine.compile(
            sourceCode, dependencies,
            compilationError -> SingleJson.error(compilationError.getMessage()),
            compilationSuccess -> {
              /* ----- notify the other instances ----- */
              JsonObject message = new JsonObject()
                .put("what", "update")
                .put("name", functionName)
                .put("kind", "kt")
                .put("code", sourceCode).put("dependencies", dependencies)
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
