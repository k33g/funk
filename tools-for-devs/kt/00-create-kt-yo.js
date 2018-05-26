#!/usr/bin/env node

const fetch = require('node-fetch');

// script.split("\n").map(item => item.trim()).join("\n")


// create
fetch("http://localhost:8080/funk/kt", {
  body: JSON.stringify({
    description:"this is the yo function",
    name: "yo",
    code: `
      import io.vertx.core.json.JsonObject
      
      fun yo(): JsonObject {
        return JsonObject().put("result", "yo")
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

