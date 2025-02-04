package edu.cs4730.firebasemessagedemo;

/**
 * Created by Seker on 4/18/2017.
 *
 * These are just constants, stored here, so if the backend server changes it's quick and easy to fix
 * them all here, instead of through out the code.
 *./get
 */

public class EndPoints {
    public static final String URL_REGISTER_DEVICE = "http://wardpi.eecs.uwyo.edu:3000/devices";
    public static final String URL_SEND_SINGLE_PUSH = "http://wardpi.eecs.uwyo.edu:3000/message/";
    public static final String URL_SEND_MULTIPLE_PUSH = "http://wardpi.eecs.uwyo.edu:3000/message";
    public static final String URL_FETCH_DEVICES = "http://wardpi.eecs.uwyo.edu:3000/devices";
}
