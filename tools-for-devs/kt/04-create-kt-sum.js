#!/usr/bin/env node

const fetch = require('node-fetch');

// create
fetch("http://localhost:8080/funk/kt", {
  body: JSON.stringify({
    description:"this is the sum function",
    name: "sum",
    code: `
      import io.vertx.core.json.JsonObject
      
      fun sum(options: Map<String, Int>): JsonObject {
        val res = options.get("a") as Int + options.get("b") as Int
        return JsonObject().put("result", res)
      }
    `.split("\n").map(item => item.trim()).join("\n")
  }),
  headers: {
    'funk-token': 'panda',
    'content-type': 'application/json'
  },
  method: 'POST'
})
.then(response => response.json())
.then(data => console.log(data))
.catch(err => console.log(err));

