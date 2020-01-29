package edu.cs4730.fitdemo;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;


/**
 * A simple example that adds "session" of a run, walk, and run.
 * you can view, add, and delete in this example.
 *
 * much of this code is from google's fit example on sessions.
 *
 */
public class SessionFragment extends Fragment {
    static final int REQUEST_OAUTH = 4;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    String TAG = "HistoryFrag";
    TextView logger;
    Button btn_View, btn_Add, btn_Update, btn_Delete;
    Handler handler;
    public static final String SAMPLE_SESSION_NAME = "Afternoon run";

    public SessionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_session, container, false);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0) {
                    Bundle stuff = msg.getData();
                    logthis(stuff.getString("logthis"));
                }
                return true;
            }
        });
        logger = myView.findViewById(R.id.loggersession);

        btn_View = myView.findViewById(R.id.btn_display_seesion);
        btn_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewSessionData();
            }
        });

        btn_Add = myView.findViewById(R.id.btn_add_session);
        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertAndVerifySession();
            }
        });

        btn_Delete = myView.findViewById(R.id.btn_delete_session);
        btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSessionData();
            }
        });

        if (hasRuntimePermissions()) {
            VerifySessionWrapper();
        } else {
            requestRuntimePermissions();
        }
        return myView;
    }


    /**
     * Returns the current state of the permissions needed.
     */
    private boolean hasRuntimePermissions() {
        int permissionState =
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRuntimePermissions() {
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.

        Log.i(TAG, "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(getActivity(),
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            REQUEST_PERMISSIONS_REQUEST_CODE);

    }


    /**
     * this is a simple wrapper method to verify authen has happened.  If yes, then
     * continue to {@link #viewSessionData()}, else request OAuth permission for the account.
     */
    private void VerifySessionWrapper() {
        if (hasOAuthPermission()) {
            viewSessionData();
        } else {
            requestOAuthPermission();
        }
    }

    /**
     * Checks if user's account has OAuth permission to Fitness API.
     */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getContext()), fitnessOptions);
    }

    /**
     * Launches the Google SignIn activity to request OAuth permission for the user.
     */
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
            this,
            REQUEST_OAUTH,
            GoogleSignIn.getLastSignedInAccount(getContext()),
            fitnessOptions);
    }

    /**
     * Gets {@link FitnessOptions} in order to check or request OAuth permission for the user.
     */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_WRITE)
            .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH) {
                //insertAndVerifySession();
                sendmessage("authenticated successful.");
            }
        }
    }


    /**
     * Creates and executes a {@link SessionReadRequest} using {@link
     * com.google.android.gms.fitness.SessionsClient} to verify the insertion succeeded .
     */
    private Task<SessionReadResponse> viewSessionData() {
        sendmessage("Reading Session API results for session: " + SAMPLE_SESSION_NAME);
        // Begin by creating the query.
        SessionReadRequest readRequest;


        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        readRequest = new SessionReadRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_SPEED)
            .setSessionName(SAMPLE_SESSION_NAME)
            .build();
        // [END build_read_session_request]

        // [START read_session]
        // Invoke the Sessions API to fetch the session with the query and wait for the result
        // of the read request. Note: Fitness.SessionsApi.readSession() requires the
        // ACCESS_FINE_LOCATION permission.
        return Fitness.getSessionsClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .readSession(readRequest)
            .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                @Override
                public void onSuccess(SessionReadResponse sessionReadResponse) {
                    // Get a list of the sessions that match the criteria to check the result.
                    List<Session> sessions = sessionReadResponse.getSessions();
                    sendmessage( "Session read was successful. Number of returned sessions is: "
                        + sessions.size());

                    for (Session session : sessions) {
                        // Process the session
                        showSession(session);

                        // Process the data sets for this session
                        List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                        for (DataSet dataSet : dataSets) {
                            showDataSet(dataSet);
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendmessage( "Failed to read session");
                }
            });
        // [END read_session]
    }


    /**
     *  Inserts and verifies a session by chaining {@link Task} form {@link #insertSessionData} and
     *  {@link #viewSessionData}.
     */
    private void insertAndVerifySession() {

        insertSessionData().continueWithTask(new Continuation<Void, Task<SessionReadResponse>>() {
            @Override
            public Task<SessionReadResponse> then(@NonNull Task<Void> task) throws Exception {
                return viewSessionData();
            }
        });
    }

    /**
     * Creates a {@link SessionInsertRequest} for a run that consists of 10 minutes running,
     * 10 minutes walking, and 10 minutes of running. The request contains two {@link DataSet}s:
     * speed data and activity segments data.
     * <p>
     * {@link Session}s are time intervals that are associated with all Fit data that falls into
     * that time interval. This data can be inserted when inserting a session or independently,
     * without affecting the association between that data and the session. Future queries for
     * that session will return all data relevant to the time interval created by the session.
     * <p>
     * Sessions may contain {@link DataSet}s, which are comprised of {@link DataPoint}s and a
     * {@link DataSource}.
     * A {@link DataPoint} is associated with a Fit {@link DataType}, which may be
     * derived from the {@link DataSource}, as well as a time interval, and a value. A given
     * {@link DataSet} may only contain data for a single data type, but a {@link Session} can
     * contain multiple {@link DataSet}s.
     */
    private SessionInsertRequest insertFitnessSession() {
        sendmessage( "Creating a new session for an afternoon run");
        // Setting start and end times for our run.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        // Set a range of the run, using a start time of 30 minutes before this moment,
        // with a 10-minute walk in the middle.
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long endWalkTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long startWalkTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -10);
        long startTime = cal.getTimeInMillis();

        // Create a data source
        DataSource speedDataSource = new DataSource.Builder()
            .setAppPackageName(getActivity().getPackageName())
            .setDataType(DataType.TYPE_SPEED)
            .setName(SAMPLE_SESSION_NAME + "- speed")
            .setType(DataSource.TYPE_RAW)
            .build();

        float runSpeedMps = 10;
        float walkSpeedMps = 3;
        // Create a data set of the run speeds to include in the session.
        DataSet speedDataSet = DataSet.create(speedDataSource);

        DataPoint firstRunSpeed = speedDataSet.createDataPoint()
            .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
        firstRunSpeed.getValue(Field.FIELD_SPEED).setFloat(runSpeedMps);
        speedDataSet.add(firstRunSpeed);

        DataPoint walkSpeed = speedDataSet.createDataPoint()
            .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
        walkSpeed.getValue(Field.FIELD_SPEED).setFloat(walkSpeedMps);
        speedDataSet.add(walkSpeed);

        DataPoint secondRunSpeed = speedDataSet.createDataPoint()
            .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
        secondRunSpeed.getValue(Field.FIELD_SPEED).setFloat(runSpeedMps);
        speedDataSet.add(secondRunSpeed);

        // [START build_insert_session_request_with_activity_segments]
        // Create a second DataSet of ActivitySegments to indicate the runner took a 10-minute walk
        // in the middle of the run.
        DataSource activitySegmentDataSource = new DataSource.Builder()
            .setAppPackageName(getActivity().getPackageName())
            .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
            .setName(SAMPLE_SESSION_NAME + "-activity segments")
            .setType(DataSource.TYPE_RAW)
            .build();
        DataSet activitySegments = DataSet.create(activitySegmentDataSource);

        DataPoint firstRunningDp = activitySegments.createDataPoint()
            .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
        firstRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
        activitySegments.add(firstRunningDp);

        DataPoint walkingDp = activitySegments.createDataPoint()
            .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
        walkingDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
        activitySegments.add(walkingDp);

        DataPoint secondRunningDp = activitySegments.createDataPoint()
            .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
        secondRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
        activitySegments.add(secondRunningDp);

        // [START build_insert_session_request]
        // Create a session with metadata about the activity.
        Session session = new Session.Builder()
            .setName(SAMPLE_SESSION_NAME)
            .setDescription("Long run around Shoreline Park")
            .setIdentifier("UniqueIdentifierHere")
            .setActivity(FitnessActivities.RUNNING)
            .setStartTime(startTime, TimeUnit.MILLISECONDS)
            .setEndTime(endTime, TimeUnit.MILLISECONDS)
            .build();

        // Build a session insert request
        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
            .setSession(session)
            .addDataSet(speedDataSet)
            .addDataSet(activitySegments)
            .build();
        // [END build_insert_session_request]
        // [END build_insert_session_request_with_activity_segments]

        return insertRequest;
    }

    /**
     * Creates and executes a {@link SessionInsertRequest} using {@link
     * com.google.android.gms.fitness.SessionsClient} to insert a session.
     */
    private Task<Void> insertSessionData() {
        //First, create a new session and an insertion request.
        SessionInsertRequest insertRequest = insertFitnessSession();

        // [START insert_session]
        // Then, invoke the Sessions API to insert the session and await the result,
        // which is possible here because of the AsyncTask. Always include a timeout when
        // calling await() to avoid hanging that can occur from the service being shutdown
        // because of low memory or other conditions.
        sendmessage( "Inserting the session in the session API");
        return Fitness.getSessionsClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .insertSession(insertRequest)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // At this point, the session has been inserted and can be read.
                    sendmessage( "Session insert was successful!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendmessage( "There was a problem inserting the session: " +
                        e.getLocalizedMessage());
                }
            });
        // [END insert_session]
    }



    /**
     * Deletes the {@link DataSet} we inserted with our {@link Session} from the History API.
     * In this example, we delete all step count data for the past 24 hours. Note that this
     * deletion uses the History API, and not the Sessions API, since sessions are truly just time
     * intervals over a set of data, and the data is what we are interested in removing.
     */
    private void deleteSessionData() {
        Log.i(TAG, "Deleting today's session data for speed");

        // Set a start and end time for our data, using a start time of 1 day before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Create a delete request object, providing a data type and a time interval
        DataDeleteRequest request = new DataDeleteRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .addDataType(DataType.TYPE_SPEED)
            .deleteAllSessions() // Or specify a particular session here
            .build();

        // Delete request using HistoryClient and specify listeners that will check the result.
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .deleteData(request)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "Successfully deleted today's sessions");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // The deletion will fail if the requesting app tries to delete data
                    // that it did not insert.
                    Log.i(TAG, "Failed to delete today's sessions");
                }
            });
        viewSessionData();
    }

    public void logthis(String item) {
        Log.i(TAG, item);
        logger.append(item + "\n");
    }


    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;
        msg.what = 0;
        handler.sendMessage(msg);

    }


    //A method to display the dataset data.  It's google method, but modified fo this example.
    private void showDataSet(DataSet dataSet) {
        sendmessage("Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            //I'm using a handler here to cheat, since I'm not in the asynctask and can't call publishprogress.
            sendmessage("Data point:");
            sendmessage("\tType: " + dp.getDataType().getName());
            sendmessage("\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            sendmessage("\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                sendmessage("\tField: " + field.getName() +
                    " Value: " + dp.getValue(field));
            }
        }
    }

    private void showSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        sendmessage("Data returned for Session: " + session.getName());
        sendmessage("Description: " + session.getDescription());
        sendmessage("Start: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS)));
        sendmessage("End: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }
}
