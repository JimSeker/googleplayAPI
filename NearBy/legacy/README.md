# Android Google Play NearBy API legacy.

Note: these are using the functioning parts of NearBy, the older Nearby protocols that were depreciated have been removed.  Hopefully, these will continue to work (about as well as they always have at least).

`NearbyMessageDemo` is an example of using the Nearby (BLE) messages APIs.  It is setup in two fragments, one that subscribes (reads) BLE messages which should be good for just about messages, including beacons.  
It will do background and foreground message reading.  The second fragment setups a simple publish message.  Best used on two phones/devices.  one publishes and the other subscribes.
NOTE, NearBy messages will be removed as of Decmember 2023.  this project has stop working and the api keys have been removed.
 

For beacons, you see the bluetooth repo for a way to continue to use bluetooth beacons and completely ignore google which a wrecked the bluetooth beacons ecosystem.  thanks google.

---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.

