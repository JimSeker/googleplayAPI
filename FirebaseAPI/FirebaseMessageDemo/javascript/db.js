"use strict";

import mariadb from "mariadb";
import dotenv from 'dotenv';
dotenv.config();

async function getConnection() {
    let conn;
    try {
        conn = await mariadb.createConnection({
            host: process.env.MDB_HOST,
            port: process.env.MDB_PORT,
            user: process.env.MDB_USER,
            password: process.env.MDB_PASS,
            database: process.env.MDB_DB,
        });
    } catch (err) {
        console.log("SQL error in establishing a connection: ", err);
        conn = null;
    } finally {
        // Close Connection
        return conn;
    };
};
function closeConnection(conn) {
    if (conn) conn.close();
}

async function doesNameExist(conn, name) {
    const rows = await conn.query("SELECT name FROM devices WHERE name = ?", [name]);
    return rows.length > 0;
}
//Get list of contacts
//add function with helper function that main code calls.
function add_data(conn, data) {
  return conn.batch("INSERT INTO devices (name, token) VALUES (?, ?) ", data);
}
//open db, add the data, then close it.
async function addData (name, token)  {
  var score = [name, token];
  let conn = await getConnection();
  if (conn) {
    if (await doesNameExist(conn, name)) {
      closeConnection(conn);
      return {error: true, message: "Device already registered"};
    } else {
      await add_data(conn, score);
      closeConnection(conn);
      return {error: false, message: "Device registered successfully"};
    }
  }
};


//helper query function for display
function get_data(conn) {
  return conn.query("SELECT id, name, token FROM devices");
}
//get the data function opens the db, gets the data, then closes the db.
async function getData ()  {
  let conn = await getConnection();
  if (conn) {
      const rows = await get_data(conn);
      closeConnection(conn);
      return rows;
  } else {
      return;
  }
};

//helper funciton to update code
async function update_data(conn, data) {
  return conn.query("UPDATE devices SET token = ? WHERE name = ?", data)
}

//actual update database code.
async function updateData (name, number)  {
  var score = [number, name];  //yes, it's reversed, from add, because order it's used.
  let conn = await getConnection();
  if (conn) {
      await update_data(conn, score);
      closeConnection(conn);
  }
};

//delete helper code
function del_data(conn, data) {
  return conn.query("DELETE FROM devices where id = ?", data);
}
//actual delete database code.
async function deleteData (id)  {
  var value = [id];  //yes, needs to be in an array.
  let conn = await getConnection();
  if (conn) {
      await del_data(conn, value);
      closeConnection(conn);
  }
};

//get the token based on the name
async function getToken (name) {
  let conn = await getConnection();
  if (conn) {
      const rows = await conn.query("SELECT token FROM devices WHERE name = ?", [name]);
      closeConnection(conn);
      return rows[0].token;
  } else {
      return;
  }
}


//only 4 funcitons are exported and can be used by the handler.js or index.js code.
export default {getData, addData, updateData, deleteData, getToken};
