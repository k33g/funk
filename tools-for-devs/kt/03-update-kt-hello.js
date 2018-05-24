#!/usr/bin/env node

const fetch = require('node-fetch');

// create
// create
fetch("http://localhost:8080/funk/kt", {
  body: JSON.stringify({
    description:"this is the sum function",
    name: "sum",
    code: `
      import io.vertx.core.json.JsonObject
      
      fun hello(): JsonObject {
        return JsonObject().put("result", "hello world")
      }
    `.split("\n").map(item => item.trim()).join("\n")
  }),
  headers: {
    'funk-token': 'panda',
    'content-type': 'application/json'
  },
  method: 'PUT'
})
.then(response => response.json())
.then(data => console.log(data))
.catch(err => console.log(err));


