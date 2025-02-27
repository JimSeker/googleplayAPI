"use strict";

const { initializeApp, applicationDefault, cert } = require('firebase-admin/app');
const { getMessaging } = require('firebase-admin/messaging');
const key = require('./serviceAccountKey.json');
const msg = require('./message');
const PROJECT_ID = 'fir-messagedemo-5f1f1';
const app = initializeApp({
    //credential: applicationDefault( ),  //to use this, must have a env variable set up, export GOOGLE_APPLICATION_CREDENTIALS="./serviceAccountKey.json"
    credential: cert(key),
    projectId: PROJECT_ID,
});


function subscribeToTopic(topic, token) {
    const registrationTokens = [token,];
    // const topic = 'news';
    // Subscribe the devices corresponding to the registration tokens to the
    // topic.

    return getMessaging(app).subscribeToTopic(registrationTokens, topic)
        .then((response) => {
            // See the MessagingTopicManagementResponse reference documentation
            // for the contents of response.
            console.log('Successfully subscribed to topic:', response);
            return { error: false, message: 'Successfully subscribed to topic' };
        })
        .catch((error) => {
            console.log('Error subscribing to topic:', error);
            return { error: true, message: 'Error subscribing to topic' };
        });

}

function unsubscribeFromTopic(topic, token) {
    const registrationTokens = [token,];
    //const topic = 'news';
    // Unsubscribe the devices corresponding to the registration tokens from the
    // topic.
    return getMessaging(app).unsubscribeFromTopic(registrationTokens, topic)
        .then((response) => {
            // See the MessagingTopicManagementResponse reference documentation
            // for the contents of response.
            console.log('Successfully unsubscribed from topic:', response);
            return { error: false, message: 'Successfully unsubscribed from topic' };
        })
        .catch((error) => {
            console.log('Error unsubscribing from topic:', error);
            return { error: true, message: 'Error unsubscribing from topic' };
        });
}

function sendTopic() {
    msg.sendTopicsMessage("news", "Test", "Hello, World!");
}

//sendTopic();
module.exports = { subscribeToTopic, unsubscribeFromTopic };