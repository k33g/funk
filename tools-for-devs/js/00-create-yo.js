#!/usr/bin/env node

const fetch = require('node-fetch');

// create
fetch("http://localhost:8080/funk/js", {
  body: JSON.stringify({
    description:"this is the yo function",
    name: "yo",
    code: `
      var JsonObject = Java.type('io.vertx.core.json.JsonObject');

      function yo() {
        return {result: "yo 👋"};
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

