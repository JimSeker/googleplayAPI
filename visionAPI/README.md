# Android Vision APIs

While these are all deprecated, Google is still using them in some examples (like VR)  and some still work perfectly, barcode and OCR

`BarocdeReader` use the googlePlay vision APIs to find a barcode and ask if you want to search Amazon or open the web page.  This a very simple example.  still works in API 33.

`FaceTrackerDemo` uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  It uses text to speech to tell you if they are open/smiling or not.  See https://github.com/googlesamples/android-vision   NOTE Vision API is deprecated for ML kit.  The landmarks have stopped working on my test phones.  This example does work in API 33.

`FaceTrackerDemo2` uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  Uses the graphic overlay to put green circles (eye open) or red x for close.  Green box for smiling, red x not smiling over the mouth.
See https://github.com/googlesamples/android-vision  NOTE Vision API is deprecated for ML kit.  The landmarks have stopped working on my test phones and this example doesn't work anymore.

`FaceTrackerMultiDemo` is based on FaceTrackerDemo2, except it uses the multiprocessors, so more then one face can be analyzed at the same time. NOTE Vision API is deprecated for ML kit.  The landmarks have stopped working on my test phones and this example does not work anymore.

`ocr-reader` is Google's example code from https://github.com/googlesamples/android-vision uses the OCR pieces of the vision APIs.  Still works in API 33

`OCRDemo` use the googlePlay vision APIs to do OCR.  It's very simple and you touch a text block to see all the text.  still works in API 33.

---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.


