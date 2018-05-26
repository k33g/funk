#!/usr/bin/env node

const fetch = require('node-fetch');

// script.split("\n").map(item => item.trim()).join("\n")


// create
fetch("http://localhost:8080/funk/js", {
  body: JSON.stringify({
    description:"this is the hello function",
    name: "hello",
    code: `
        function hello() {
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
