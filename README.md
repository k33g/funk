# FunK

**FunK** is a **scalable** function provider (think to a kind of small FaaS)

> :wave: **FunK** is "cloud provider **agnostic**", you can deploy **FunK** where you want. You don't need Kubernetes or Docker, but you can if you want.

**FunK** provides:

- JavaScript functions
- Kotlin functions

> **FunK** is made with :heart: with the :sparkles: [RedPipe](http://redpipe.net) *(Vert.x based)*

:warning: don't use **FunK** in production: for now, it's a proof of concept

**FunK** is a [Bots.Garden](http://www.bots.garden/) project

## Prerequisites

- Java 8
- Redis database
- Maven

## Run it

> Simple way (locally):

```shell
# you need to start `redis-server` before
# you need to define a token

FUNK_TOKEN="panda" mvn install exec:java
```


> Start with an other http port:

```shell
FUNK_TOKEN="panda" PORT=8080 mvn install exec:java
```

> Change the Redis information:

```shell
FUNK_TOKEN="panda" \
PORT=8080 \
REDIS_PORT=6379 \
REDIS_HOST="localhost" \
REDIS_PASSWORD="password" \
mvn install exec:java
```

> On a cloud provider, inside a virtual machine or a container:
> - if the FunK server is listening on 8080
> - and you are forwarding on 80
> you need to use this:
> - `PORT=8080`
> - `SERVICE_PORT=80`
> - `SERVICE_HOST="your.domain.name"`
>
> you will access to your FunK server like that: [http://your.domain.name](http://your.domain.name)

```shell
# if the server is listening on 8080, but 80 is exposed
FUNK_TOKEN="panda" \
PORT=8080 \
REDIS_PORT=6379 \
REDIS_HOST="localhost" \
REDIS_PASSWORD="password" \
SERVICE_HOST="localhost" \
SERVICE_PORT=80 \
mvn install exec:java
```

## Functions

Each function, for a language, has a unique identifier: **its name**, so

- :warning: you can create only one `hello` JavaScript function
  - :wave: btw, it's convenient if you want to call various versions from an other function
- but you can create one `hello` JavaScript function and one `hello` Kotlin function
- if you need several version use something like that: `hello_v_101`
 

### JavaScript functions

#### Create a JavaScript function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://localhost:8080/funk/js \
-d @- <<'EOF'
{
  "description":"yo function",
  "name":"yo",
  "code":"function yo(options){
    return {result: options.message};
  }"
}
EOF
```

> **Remarks**:
> - the FunK JavaScript functions take only one parameter: `options`, this is an object (eg: `{a:40, b:2}`)
> - you need to return a JavaScript object (`{}`) with a `result` member
> - this is a `POST` request
> - you need to provide the token in the header: `--header "funk-token: panda"` 
> - :warning: the `name` and the name of the function in the body have to be the same
> - :warning: note the end of the url: `/funk/js`

#### Run a JavaScript function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://localhost:8080/funk/js/run \
-d @- <<'EOF'
{
  "name":"yo",
  "parameters":{
    "message": "hello world"
  }
}
EOF
```

> you'll get: `{"result":"hello world"}`

> **Remarks**:
> - you need to pass the parameters thanks to an object using `parameters`, eg: `parameters:{a:40, b:2}`
> - this is a `POST` request


#### Update a JavaScript function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X PUT http://localhost:8080/funk/js \
-d @- <<'EOF'
{
  "description":"yo function",
  "name":"yo",
  "code":"function yo(options){
    return {result: options.message};
  }"
}
EOF
```

> **Remarks**:
> - this is a `PUT` request

#### Get the list of the JavaScript function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X GET http://localhost:8080/funk/js
```

### Kotlin functions

#### Create a Kotlin function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://localhost:8080/funk/kt \
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


> **Remarks**:
> - the FunK Kotlin functions take only one parameter: `options`, this is a `Map<String, Object>`
> - you need to return a `JsonObject` with a `result` member
>   - so you need to import this: `io.vertx.core.json.JsonObject`
> - this is a `POST` request
> - you need to provide the token in the header: `--header "funk-token: panda"` 
> - :warning: the `name` and the name of the function in the body have to be the same
> - :warning: note the end of the url: `/funk/kt`

#### Run a Kotlin function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X POST http://localhost:8080/funk/kt/run \
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

#### Update a Kotlin function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X PUT http://localhost:8080/funk/kt \
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

#### Get the list of the Kotlin function

```shell
curl --header "funk-token: panda" -H "Content-Type: application/json" -X GET http://localhost:8080/funk/kt
```


## TODO

- JavaScript Client
- JavaClient
- Kotlin Client
- other clients
- security
- administration console
- backup management
- add the ability to use files locally and in an object storage
- add the ability to use files from a source control management system
- https
- logs management
- function deletion
- ...
