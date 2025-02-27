"use strict";
const axios = require('axios');

const p7 = "fXLCxRMDRKOJN_jZDK7nuI:APA91bEdosD1qhZTT6h88GlRntYOuJrSDQCc2NXJcBykIUFaN8YuRZw0s3fG2bcUPVZkSUYoz1hgZ7PbYfzmeDtzwXix02SyFwV6ZJcDthunmlHOtGL_7GQ";
const p8 = "f3-FmgFOR_qZkiQwX_e9A5:APA91bFs67vL8aYlA5QqHUpeusDx5LMX1qytPqv316goaU_-tnAlZ5QTPU4nDiVZ23GFKdo-C7AIJS3dXNn6cFWEQw-g67UWPD8wnXF43JDs8w_30Tzc2vlbcwCc7cU_vzg4V_bTxSt9";

// axios.post('http://localhost:3000/subscribe/news',    { token: p7 })
// .then(function (response) { console.log(response.data); })
// .catch(function (error) { console.log(error); });


// axios.post('http://localhost:3000/unsubscribe/news',    { token: p7 })
// .then(function (response) { console.log(response.data); })
// .catch(function (error) { console.log(error); });

axios.post('http://localhost:3000/topic/news',    { title: "RTest 3", message: "Hello, World!" })
.then(function (response) { console.log(response.data); })
.catch(function (error) { console.log(error); });