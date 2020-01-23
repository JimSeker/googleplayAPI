# Android Google Play API Demos

<b>legacy/</b> older examples that are no longer updated.

<b>ActMapDemo</b> combines LocationAware, ActivityRecognition, and Google Maps together in an example where it use the location to draw on a map the path the user is 
going.  The line changes color to show the different activities (ie walking, running, driving, etc).  A key is needed on the map for the colors though.

<b>AcitivtyRecognition</b> is google's example code from https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition and will should likely be removed from this repo

<b>ActivityRecognitionDemo</b> uses the Activity Recognition API provide in the GooglePlay APIs.  This example show the mostl likel activity (and speech), plus any activities that "it" thinks are at least 50% likely.  


<b>GoogleLoginDemo</b> is a example of how to use the new login APIs.  This example is so very basic that is doesn't do anything other then login.  As note, if you run this example of two devices with the same user and login on one device, then start the activity on the second device, it will already be logged in.

<b>LocationAwareDemo</b> shows how to use the APIs to do location better then standard GPS demos.  plus will show the likely address of your location.

<b>FitDemo</b> Currently demo's the Sensor APIs of Google Fit. This is example is based on https://github.com/googlesamples/android-fit  Note you will need a key to run this demo, see https://developers.google.com/fit/android/get-api-key#release-cert to create one you can use. 

<b>FaceTrackerDemo</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  It uses text to speech to tell you if they are open/smiling or not.  See https://github.com/googlesamples/android-vision   NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>FaceTrackerDemo2</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  Uses the graphic overlay to put green circles (eye open) or red x for close.  Green box for smiling, red x not smiling over the mouth.
See https://github.com/googlesamples/android-vision  NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>FaceTrackerMultiDemo</b> is based on FaceTrackerDemo2, except it uses the multiprocessors, so more then one face can be analysed at the same time. NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>MLFaceTrackerDemo</a> is the same as the FaceTracker2 and FaceTrackerMultiDemo, but is based on the ML Kit code..  Note this is firebase, so you need to add to a project you own.

<b>BarocdeReader</b> use the googlePlay vision APIs to find a barcode and ask if you want to search Amazon or open the web page.  This a very simple example.  

<b>ocr-reader</b> is Google's example code from https://github.com/googlesamples/android-vision uses the OCR pieces of the vision APIs.

<b>OCRDemo</b> use the googlePlay vision APIs to do OCR.  It's very simple and you touch a text block to see all the text.

<b>adMobDemo</b> is a very simple demo of using the Admob API.  Please see https://developers.google.com/admob/android/quick-start for documentation and https://github.com/googleads/googleads-mobile-android-examples/releases for more examples.

<b>FirebaseMessageDemo</b> is a start at an example for Cloud Messaging.  It works pretty well.  The webcode needed is in the php directory of the project, you will need a webserver to get this project running.

<b>fbDatabaseAuthDemo</b> Has multiple fragments to show firebase authentication, Google Authentication, Realtime Database (and firebase adapter for recyclerview), Storage, remote config, and invites (which sort of work).  There is code for notifications and analytics as well.

<b>firebaseMLKit</b> is a first attempt at using the firebase ML kit to do image recognition.  It's using mostly google example code at this point.


<b>NearbyConnectionDemo</b> is simple example of using the Nearby Connection APIs.  It connects up between 2 devices and sends two messages.  It's still a little rough and needs much better comments.

<b>NearbyMessageDemo</b> is an example of using the Nearby (BLE) messages APIs.  It is setup in two fragments, one that subscribes (reads) BLE messages which should be good for just about messages, including beacons.  
It will do background and foreground message reading.  The second fragment setups a simple publish message.  Best used on two phones/devices.  one publishes and the other subscribes.

<b>NearbyConStreamDemo</b> is an example of file passing to create a 4 fps video "stream" from one device to the other device.  It takes pictures and sends them.  

<b>Mapdemov2</b>  is a demo of how to use the maps V2.  There are three fragments first two show different types of maps.  third, how to draw on the maps.

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and Cosc 4735 Advanced Mobile Programming course.
All examples are for Android.

