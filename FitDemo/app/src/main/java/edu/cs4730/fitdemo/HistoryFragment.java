package edu.cs4730.fitdemo;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple example of how to use the history API.
 */
public class HistoryFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    Handler handler;

    static final int REQUEST_OAUTH = 3;
    String TAG = "HistoryFrag";
    TextView logger;
    Button btn_ViewWeek, btn_ViewToday, btn_AddSteps, btn_UpdateSteps, btn_DeleteSteps;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create the client needed.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                        //have it login the user instead of us doing it manually.
                .enableAutoManage(getActivity(), REQUEST_OAUTH, this)
                .build();
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
        logger = (TextView) myView.findViewById(R.id.loggerh);

        btn_ViewWeek = (Button) myView.findViewById(R.id.btn_view_week);
        btn_ViewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ViewWeekStepCountTask().execute();
            }
        });

        btn_ViewToday = (Button) myView.findViewById(R.id.btn_view_today);
        btn_ViewToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ViewTodaysStepCountTask().execute();
            }
        });

        btn_AddSteps = (Button) myView.findViewById(R.id.btn_add_steps);
        btn_AddSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddStepsToGoogleFitTask().execute();
            }
        });

        btn_UpdateSteps = (Button) myView.findViewById(R.id.btn_update_steps);
        btn_UpdateSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateStepsOnGoogleFitTask().execute();
            }
        });

        btn_DeleteSteps = (Button) myView.findViewById(R.id.btn_delete_steps);
        btn_DeleteSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // new DeleteYesterdaysStepsTask().execute();  //this one uses an asynctask
                DeleteYesterdaysStepsResultcallBack();  //this one uses a resultcallback() method.
            }
        });

        return myView;
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

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {

        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        logthis("OnConnected!");

    }

    @Override
    public void onConnectionSuspended(int i) {
        logthis("OnConnectionSuspected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logthis("onConnectionFailed cause:" + connectionResult.toString());
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

    //this method is called from many of the aysnctask below, so it is all here instead
    //This method will list the step data for the last week
    private void displayLastWeeksData() {
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
        //now submit the request.
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            sendmessage("Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            sendmessage("Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }
    }

    //Asnyc Task to complete the choices


    //
    //This one is very simple, because it is only today information
    // A note, that step count doesn't actually require authenication, so it can be easily used for wear
    //  In use, call this every 30 seconds in active mode, 60 in ambient for watch faces
    class ViewTodaysStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            DailyTotalResult result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
            showDataSet(result.getTotal());
            return null;
        }
    }

    //show the step count for the last 7 days.
    class ViewWeekStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksData();
            return null;
        }
    }

    //add data into the fit system.
    //Note, you can't over lapping data, it will be ignored, so use update if data already exists for this time period.
    class AddStepsToGoogleFitTask extends AsyncTask<Void, String, Void> {
        protected Void doInBackground(Void... params) {
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
                    .setName("Step Count")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            int stepCountDelta = 10000;  //we will add 10,000 steps for yesterday.
            DataSet dataSet = DataSet.create(dataSource);

            DataPoint point = dataSet.createDataPoint()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            point.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
            dataSet.add(point);

            com.google.android.gms.common.api.Status status = Fitness.HistoryApi.insertData(mGoogleApiClient, dataSet).await(1, TimeUnit.MINUTES);

            if (!status.isSuccess()) {
                publishProgress("Problem with inserting data: " + status.getStatusMessage());
            } else {
                publishProgress("data inserted");
            }

            displayLastWeeksData();
            return null;
        }

        protected void onProgressUpdate(String... item) {
            logthis(item[0]);
        }
    }

    //This will update the existing data and add the step count.
    class UpdateStepsOnGoogleFitTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
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
                    .setName("Step Count")
                    .setType(DataSource.TYPE_RAW)
                    .build();

            int stepCountDelta = 20000;  //add another 20K steps to the data already there.
            DataSet dataSet = DataSet.create(dataSource);

            DataPoint point = dataSet.createDataPoint()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            point.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
            dataSet.add(point);

            DataUpdateRequest updateRequest = new DataUpdateRequest.Builder().setDataSet(dataSet).setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS).build();
            Fitness.HistoryApi.updateData(mGoogleApiClient, updateRequest).await(1, TimeUnit.MINUTES);

            displayLastWeeksData();
            return null;
        }
    }

    //simple function to delete data.  this case the step_count for today.
    class DeleteYesterdaysStepsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            long startTime = cal.getTimeInMillis();

            DataDeleteRequest request = new DataDeleteRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .build();

            Fitness.HistoryApi.deleteData(mGoogleApiClient, request).await(1, TimeUnit.MINUTES);

            displayLastWeeksData();
            return null;
        }
    }

    //using a resultcallback, we don't need to use an ansyctask/thread.
    //not the result is NOT on the main thread.
    void DeleteYesterdaysStepsResultcallBack() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();
        Fitness.HistoryApi.deleteData(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            sendmessage("Successfully deleted today's step count");
                        } else {
                            sendmessage("FAILED to delete today's step count");
                        }
                        //wait, we are on the UI thread!  doc's say we are not supported to be.
                        new ViewWeekStepCountTask().execute();
                        //displayLastWeeksData();
                    }
                });
    }
}
