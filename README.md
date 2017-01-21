# Android Google Play API Demos

<b>ActMapDemo</b> combines LocationAware, ActivityRecognition, and Google Maps together in an example where it use the location to draw on a map the path the user is 
going.  The line changes color to show the different activities (ie walking, running, driving, etc).  A key is needed on the map for the colors though.

<b>AcitivtyRecognition</b> is google's example code from https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition and will should likely be removed from this repo

<b>ActivityRecognitionDemo</b> uses the Activity Recognition API provide in the GooglePlay APIs.  This example show the mostl likel activity (and speech), plus any activities that "it" thinks are at least 50% likely.  


<b>GoogleLoginDemo</b> is a example of how to use the new login APIs.  This example is so very basic that is doesn't do anything other then login.  As note, if you run this example of two devices with the same user and login on one device, then start the activity on the second device, it will already be logged in.

<b>LocationAwareDemo</b> shows how to use the APIs to do location better then standard GPS demos.  plus will show the likely address of your location.

<b>FitDemo</b> Currently demo's the Sensor APIs of Google Fit.  More is planned for this demo.  Note you will need a key to run this demo, see https://developers.google.com/fit/android/get-api-key#release-cert to create one you can use. 

<b>FaceTrackerDemo</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  It uses text to speech to tell you if they are open/smiling or not.  See https://github.com/googlesamples/android-vision 

<b>FaceTrackerDemo2</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  Uses the graphic overlay to put green circles (eye open) or red x for close.  Green box for smiling, red x not smiling over the mouth.
See https://github.com/googlesamples/android-vision 

<b>FaceTrackerMultiDemo</b> is based on FaceTrackerDemo2, except it uses the multiprocessors, so more then one face can be analysed at the same time.

<b>BarocdeReader</b> use the googlePlay vision APIs to find a barcode and ask if you want to search Amazon or open the web page.  This a very simple example.  

<b>adMobDemo</b> is a very simple demo of using the Admob API.  Please see https://developers.google.com/admob/android/quick-start for documentation and https://github.com/googleads/googleads-mobile-android-examples/releases for more examples.

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and Cosc 4735 Advanced Mobile Programming course.
All examples are for Android.

