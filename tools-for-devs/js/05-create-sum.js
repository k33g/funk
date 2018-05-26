#!/usr/bin/env node

const fetch = require('node-fetch');

// script.split("\n").map(item => item.trim()).join("\n")


// create
fetch("http://localhost:8080/funk/js", {
  body: JSON.stringify({
    description:"this is the sum function",
    name: "sum",
    code: `
      var JsonObject = Java.type('io.vertx.core.json.JsonObject');

      function sum(options) {
        print(options)
        return {result: options.a + options.b};
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

