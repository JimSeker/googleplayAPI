"use strict";
const express = require('express');
const bodyParser = require('body-parser');
require("dotenv").config()
const db = require('./db');

const app = express();
const port = process.env.PORT || 3000;
const msg = require('./message');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

// REST API to add an item
app.post('/devices', async (req, res) => {
    console.log("devices post");
    const { name, token } = req.body;
    console.log(name, token);
    if (name == '' || token == '') {
        res.status(200).json({ error: true, message: 'Invalid request' });
        console.log("invalid request");
        return;
    }
    try {
        const ret = await db.addData(name, token);
        res.status(201).json(ret);
    } catch (err) {
        res.status(200).json({ error: true, message: 'Device not registered' });
        console.log("device not registered");
    }
});

// REST API to get all items
app.get('/devices', async (req, res) => {
    console.log("devices get");
    try {
        const items = await db.getData();
        res.status(200).json({ error: false, devices: items });
    } catch (err) {
        res.status(500).json({ error: 'Failed to get items' });
    }
});

// REST API to update an item
app.put('/devices/:name', async (req, res) => {
    const { name } = req.params;
    const { token } = req.body;
    if (token == '') {
        res.status(400).json({ error: 'Invalid input' });
        return;
    }
    try {
        await db.updateData(name, token);
        res.status(200).json({ name, token });
    } catch (err) {
        res.status(500).json({ error: 'Failed to update item' });
    }
});

// REST API to delete an item
app.delete('/devices/:id', async (req, res) => {
    const { id } = req.params;
    try {
        await db.deleteData(id);
        res.status(200).json({ message: 'Item deleted successfully' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to delete item' });
    }
});

//messaging api 
app.post('/message/:name', async (req, res) => {
    const { name } = req.params;
    const { title, message } = req.body;
    console.log("msg single",name, title, message);
    if (title == '' || message == '' || name == '') {
        console.log("invalid input");
        res.status(400).json({ error: 'Invalid input' });
        return;
    }
    console.log("sending message");
    try {
     msg.sendSingleMessage(name, title, message);
        res.status(200).json({ message: 'Message sent successfully' });
    } catch (err) {
        res.status(200).json({ error: 'Failed to send message' });
    }
});

//send message to all users
app.post('/message', async (req, res) => {
    const { title, message } = req.body;
    if (title == '' || message == '') {
        res.status(400).json({ error: 'Invalid input' });
        return;
    }
    try {
       msg.sendMultipleMessages(title, message);
        res.status(200).json({ message: 'Message sent successfully' });
    } catch (err) {
        res.status(200).json({ error: 'Failed to send message' });
    }
});

app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});


