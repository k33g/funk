# Deploy FunK on Clever Cloud

Cleve-Clous is a PaaS: [https://www.clever-cloud.com/](https://www.clever-cloud.com/)

## On the Clever Cloud side

- create an application (you need to be registered), eg: *Create an application from a Github repository* if your project is on GitHub
- it's a *Java+Maven* application
- click on *Create*
- select a *Redis* add-on
- click on *Next*
- Setup the environment variables:

```shell
MAVEN_DEPLOY_GOAL=-Djava.security.manager -Djava.security.policy=./funk.policy install exec:java
JAVA_VERSION=8
FUNK_TOKEN=panda
PORT=8080
SERVICE_HOST=funk.cleverapps.io
SERVICE_PORT=80
```

- click on *Next*
- wait ...
- add the domain name: `funk.cleverapps.io`

## Create your first function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://funk.cleverapps.io/funk/kt \
-d @- <<'EOF'
{
  "description":"sum function",
  "name":"sum",
  "code":"import io.vertx.core.json.JsonObject\n
    fun sum(options: Map<String, Int>): JsonObject {\n
      val res = options.get(\"a\") as Int + options.get(\"b\") as Int\n
      return JsonObject().put(\"result\", res)\n
    }
  "
}
EOF
```

> the first compilation could take some seconds

## Run your first function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://funk.cleverapps.io/funk/kt/run \
-d @- <<'EOF'
{
  "name":"sum",
  "parameters":{
    "a": 38,
    "b": 4
  }
}
EOF
```