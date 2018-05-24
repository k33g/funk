package garden.bots.token;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import rx.Single;

import java.util.Optional;

public class Check {
  public static String funkToken() {
    return Optional.ofNullable(System.getenv("FUNK_TOKEN")).orElse("NO_TOKEN");
  }

  public static Try<Boolean> token(String token) {
    return funkToken().equals(token) || funkToken().equals("NO_TOKEN") ? Try.success(true) : Try.failure(new Exception("bad token"));
  };


  public static Single<JsonObject> token(String token, Function0<Single<JsonObject>> failure, Function0<Single<JsonObject>> success) {
    return funkToken().equals(token) || funkToken().equals("NO_TOKEN") ? success.apply() : failure.apply();
  };

}
