# Android Google Play NearBy API

Note: these are using the functioning parts of NearBy, the older Nearby protocols that were depreciated have been removed.  Hopefully, these will continue to work (about as well as they always have at least).

`NearbyConnectionDemo` is simple example of using the Nearby Connection APIs.  It connects up between 2 devices and sends two messages.  It's still a little rough and needs much better comments.

`NearbyMessageDemo` is an example of using the Nearby (BLE) messages APIs.  It is setup in two fragments, one that subscribes (reads) BLE messages which should be good for just about messages, including beacons.  
It will do background and foreground message reading.  The second fragment setups a simple publish message.  Best used on two phones/devices.  one publishes and the other subscribes.
NOTE, this code no longer seems to work in API 30+  I'm unable to find a reason why 2 phones can't publish/subcribe to the messages.


`NearbyConStreamDemo` is an example of file passing to create a 4 fps video "stream" from one device to the other device.  It takes pictures and sends them.  

---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.

