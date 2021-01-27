package edu.cs4730.androidbeaconlibrarydemo2;

public class BeaconData {
    int serviceUuid;
    int BeaconTypeCode;
    String BeaconType;
    boolean telemetry;
    String mURL;
    double distance;
    BeaconData() {
        serviceUuid =0; BeaconTypeCode =0; telemetry = false;
        mURL = ""; distance =0.0;
    }

    BeaconData(int uuid, int type, int dist) {
        serviceUuid =uuid; BeaconTypeCode =type;
        telemetry = false; mURL = "";
        distance = dist;
    }

}
