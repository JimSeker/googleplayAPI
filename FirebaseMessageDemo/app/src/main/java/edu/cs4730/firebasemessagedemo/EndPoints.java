package edu.cs4730.firebasemessagedemo;

/**
 * Created by Seker on 4/18/2017.
 *
 * These are just constants, stored here, so if the backend server changes it's quick and easy to fix
 * them all here, instead of through out the code.
 *
 */

public class EndPoints {
    public static final String URL_REGISTER_DEVICE = "http://10.216.218.12/FcmDemo/RegisterDevice.php";
    public static final String URL_SEND_SINGLE_PUSH = "http://10.216.218.12/FcmDemo/sendSinglePush.php";
    public static final String URL_SEND_MULTIPLE_PUSH = "http://10.216.218.12/FcmDemo/sendMultiplePush.php";
    public static final String URL_FETCH_DEVICES = "http://10.216.218.12/FcmDemo/GetRegisteredDevices.php";
}
