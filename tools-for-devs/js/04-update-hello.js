#!/usr/bin/env node

const fetch = require('node-fetch');

// script.split("\n").map(item => item.trim()).join("\n")


// create
fetch("http://localhost:8080/funk/js", {
  body: JSON.stringify({
    description:"this is the hello function",
    name: "hello",
    code: `
      var Funk = Java.type('funk.Funk');
      var JsonObject = Java.type('io.vertx.core.json.JsonObject');

      function hello() {
        
        var sumParameters = new JsonObject()
        sumParameters.put("a", 40)
        sumParameters.put("b", 2)
        var sumJS = Funk.js("sum").call(sumParameters)
        
        print("SUM JS: " + sumJS)
        
        var sumKT = Funk.kt("sum").call(sumParameters)
        
        print("SUM KT: " + sumKT)
        
        
        print("KT:" + Funk.kt("yo").call())
        print("JS:" + Funk.js("yo").call())
        
        
        return {result: "Hello 👋 World 🌍"};
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

