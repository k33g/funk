#!/usr/bin/env node

const fetch = require('node-fetch');

// run
fetch("http://localhost:8080/funk/js/run", {
  body: JSON.stringify({
    name: "hello"
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


