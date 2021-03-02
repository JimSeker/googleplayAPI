# Android Vision APIs

While these are all decpricated, google is still using them in some examples (like VR)  and some still work perfectly, barcode and OCR

<b>BarocdeReader</b> use the googlePlay vision APIs to find a barcode and ask if you want to search Amazon or open the web page.  This a very simple example.  

<b>FaceTrackerDemo</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  It uses text to speech to tell you if they are open/smiling or not.  See https://github.com/googlesamples/android-vision   NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>FaceTrackerDemo2</b> uses the GooglePlay vision APIs to track see if the user has the eyes open and is smiling.  Uses the graphic overlay to put green circles (eye open) or red x for close.  Green box for smiling, red x not smiling over the mouth.
See https://github.com/googlesamples/android-vision  NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>FaceTrackerMultiDemo</b> is based on FaceTrackerDemo2, except it uses the multiprocessors, so more then one face can be analysed at the same time. NOTE Vision API is depricated for ML kit.  The landmarks have stopped working on my test phones.

<b>ocr-reader</b> is Google's example code from https://github.com/googlesamples/android-vision uses the OCR pieces of the vision APIs.

<b>OCRDemo</b> use the googlePlay vision APIs to do OCR.  It's very simple and you touch a text block to see all the text.

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and Cosc 4735 Advanced Mobile Programming course.
All examples are for Android.

