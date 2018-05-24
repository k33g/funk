
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



const fetch = require('node-fetch');

// create
fetch("http://localhost:8080/funk/js", {
    body: JSON.stringify({
        description:"hi function",
        name: "hi",
        code: `
        function hi(options){
          print(options);
          return {result:{answer:1000*options.count}};
        }
        `
    }),
    headers: {
        'funk-token': 'azerty',
        'content-type': 'application/json'
    },
    method: 'POST'
})
    .then(response => response.json())
    .then(data => console.log(data))


fetch("http://localhost:8080/funk/js/run", {
    body: JSON.stringify({
        name: "hello",
        parameters: {
            message:"hello world",
            count: 42
        }

    }),
    headers: {
        'funk-token': 'azerty',
        'content-type': 'application/json'
    },
    method: 'POST'
}).then(response => response.json()).then(data => console.log(data))

fetch("http://localhost:8080/funk/js/run", {
    body: JSON.stringify({
        name: "hello",
        parameters: {
            message:"hi world",
            count: 2000
        }

    }),
    headers: {
        'funk-token': 'azerty',
        'content-type': 'application/json'
    },
    method: 'POST'
}).then(response => response.json()).then(data => console.log(data))

fetch("http://localhost:8080/funk/js/run", {
    body: JSON.stringify({
        name: "yop",
        parameters: {
            message:"hi world",
            count: 2000
        }

    }),
    headers: {
        'funk-token': 'azerty',
        'content-type': 'application/json'
    },
    method: 'POST'
}).then(response => response.json()).then(data => console.log(data))

// list of javascript functions
fetch("http://localhost:8080/funk/js", {
    headers: {
        'funk-token': 'azerty',
        'content-type': 'application/json'
    }
}).then(response => response.json()).then(data => console.log(data))