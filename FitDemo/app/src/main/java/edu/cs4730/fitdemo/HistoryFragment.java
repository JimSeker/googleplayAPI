package edu.cs4730.fitdemo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple example of how to use the history API adding, updating, and removing steps.
 */
public class HistoryFragment extends Fragment {


    Handler handler;

    static final int REQUEST_OAUTH = 3;
    String TAG = "HistoryFrag";
    TextView logger;
    Button btn_ViewWeek, btn_ViewToday, btn_AddSteps, btn_UpdateSteps, btn_DeleteSteps;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_history, container, false);
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
        logger = myView.findViewById(R.id.loggerh);

        btn_ViewWeek = myView.findViewById(R.id.btn_view_week);
        btn_ViewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new ViewWeekStepCountTask().execute();
                displayLastWeeksData();
            }
        });

        btn_ViewToday = myView.findViewById(R.id.btn_view_today);
        btn_ViewToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTodayData();
            }
        });

        btn_AddSteps = myView.findViewById(R.id.btn_add_steps);
        btn_AddSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertAndDisplayToday();
            }
        });

        btn_UpdateSteps = myView.findViewById(R.id.btn_update_steps);
        btn_UpdateSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAndDisplayWeek();
            }
        });

        btn_DeleteSteps = myView.findViewById(R.id.btn_delete_steps);
        btn_DeleteSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStepsYesterday();
            }
        });


        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.

        // insertAndVerifySessionWrapper();
        //make sure we have permission to read and change the step data
        //note, if you doing other things like run with location data, you will fine_access permissions too.
        if (!hasOAuthPermission()) {
            requestOAuthPermission();
        }
        return myView;
    }


    /*
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
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH) {
                displayLastWeeksData();
            }
        }
    }

    //This one is very simple, because it is only today information
    // A note, that step count doesn't actually require authentication, so it can be easily used for wear
    private Task<DataSet> displayTodayData() {
        Log.i(TAG, "Reading History API results for today of Steps");
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                @Override
                public void onSuccess(DataSet dataSet) {
                    Log.i(TAG, "Reading History API results for today");
                    showDataSet(dataSet);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "Failed to read DailyTotal for Steps.");
                }
            });

    }

    /**
     * Creates and executes a {@link SessionReadRequest} using {@link
     * com.google.android.gms.fitness.SessionsClient} to verify the insertion succeeded .
     */
    private Task<DataReadResponse> displayLastWeeksData() {
        // Begin by creating the query.

        Log.i(TAG, "Reading History API results for last 7 days of Steps");
        //First create the DataReadRequest data.
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        //show the dates requested
        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        sendmessage("Range Start: " + dateFormat.format(startTime));
        sendmessage("Range End: " + dateFormat.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build();


        // Now we can return the task object which will run.
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            //.readSession(readRequest)
            .readData(readRequest)
            .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                @Override
                public void onSuccess(DataReadResponse dataReadResponse) {
                    // Get a list of the sessions that match the criteria to check the result.
                    //Used for aggregated data
                    if (dataReadResponse.getBuckets().size() > 0) {
                        sendmessage("Number of buckets: " + dataReadResponse.getBuckets().size());
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                showDataSet(dataSet);
                            }
                        }
                    }
                    //Used for non-aggregated data
                    else if (dataReadResponse.getDataSets().size() > 0) {
                        sendmessage("Number of returned DataSets: " + dataReadResponse.getDataSets().size());
                        for (DataSet dataSet : dataReadResponse.getDataSets()) {
                            showDataSet(dataSet);
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendmessage("Failed to read DataResponse.");
                }
            });

    }


    /**
     * Inserts and verifies a session by chaining {@link Task} form {@link #insertSteps} and
     * {@link #displayTodayData()}.
     */
    private void insertAndDisplayToday() {

        insertSteps().continueWithTask(new Continuation<Void, Task<DataSet>>() {
            @Override
            public Task<DataSet> then(@NonNull Task<Void> task) throws Exception {
                return displayTodayData();
            }
        });
    }


    /**
     * Creates and executes a insert Data request for 10,000 steps for today.
     */
    private Task<Void> insertSteps() {
        //First, create a new session and an insertion request.
        //Adds steps spread out evenly from start time to end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
            .setAppPackageName(getActivity())
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setStreamName("Step Count")
            .setType(DataSource.TYPE_RAW)
            .build();

        int stepCountDelta = 10000;  //we will add 10,000 steps for yesterday.


        /* before v17 it was this.
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint point = dataSet.createDataPoint()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        point.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);

        dataSet.add(point);
*/
        //version 18+
        DataPoint point = DataPoint.builder(dataSource)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_STEPS, stepCountDelta)
            .build();

        DataSet dataSet = DataSet.builder(dataSource)
            .add(point)
            .build();


        // Now insert the new dataset view the client.
        Log.i(TAG, "Inserting the session in the History API");
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .insertData(dataSet)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // At this point, the session has been inserted and can be read.
                    sendmessage("dataSet of 10000 steps inserted successfully!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendmessage("There was a problem inserting the dataset: " +
                        e.getLocalizedMessage());
                }
            });

    }


    /*
     * This is a wrapper function to update yesterdays data and display this weeks data.
     */
    private void UpdateAndDisplayWeek() {

        insertUpdateSteps().continueWithTask(new Continuation<Void, Task<DataReadResponse>>() {
            @Override
            public Task<DataReadResponse> then(@NonNull Task<Void> task) throws Exception {
                return displayLastWeeksData();
            }
        });
    }


    /**
     * Creates and executes a This will update the existing data and add the step count.
     */
    private Task<Void> insertUpdateSteps() {
        //If two entries overlap, the new data is dropped when trying to insert. Instead, you need to use update
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
            .setAppPackageName(getActivity())
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setStreamName("Step Count")
            .setType(DataSource.TYPE_RAW)
            .build();

        int stepCountDelta = 20000;  //add another 20K steps to the data already there.

        //create the point to update.
        DataPoint point = DataPoint.builder(dataSource)
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .setField(Field.FIELD_STEPS, stepCountDelta)
            .build();
        //now add it to the new data set.
        DataSet dataSet = DataSet.builder(dataSource)
            .add(point)
            .build();

        DataUpdateRequest updateRequest = new DataUpdateRequest.Builder().setDataSet(dataSet).setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS).build();

        // Now insert the new dataset view the client.
        Log.i(TAG, "Inserting the session in the History API");
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .updateData(updateRequest)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // At this point, the session has been inserted and can be read.
                    sendmessage("dataSet of 20000 steps updated for yesterday successfully!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendmessage("There was a problem updating yesterdays dataset: " +
                        e.getLocalizedMessage());
                }
            });

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

    /**
     * Deletes a {@link DataSet} from the History API. In this example, we delete all step count data
     * for the past 24 hours.
     */
    private void deleteStepsYesterday() {
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
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .deleteAllSessions() // Or specify a particular session here
            .build();

        // Delete request using HistoryClient and specify listeners that will check the result.
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .deleteData(request)
            .addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendmessage("Successfully deleted today's step count data.");
                        } else {
                            sendmessage("Failed to delete today's step count data." + task.getException());
                        }
                    }
                });

        displayLastWeeksData();
    }


    //A method to display the dataset data.  It's google method, but modified fo this example.
    private void showDataSet(DataSet dataSet) {
        sendmessage("Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

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
}
