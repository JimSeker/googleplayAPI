//just a test file, ignore this.

const db = require('./db');

db.getData().then((data) => {
    console.log(data);
}).catch((err) => { console.log(err); });
