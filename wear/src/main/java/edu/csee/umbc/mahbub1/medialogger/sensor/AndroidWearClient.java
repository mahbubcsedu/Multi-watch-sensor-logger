package edu.csee.umbc.mahbub1.medialogger.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import edu.csee.umbc.mahbub1.medialogger.WearApp;
import edu.csee.umbc.mahbub1.medialogger.common.data.Data;
import edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch;
import edu.csee.umbc.mahbub1.medialogger.common.data.DataRequestResponse;
import edu.csee.umbc.mahbub1.medialogger.common.data.WearableSensorDataList;
import edu.csee.umbc.mahbub1.medialogger.common.utils.DataMapFields;
import edu.csee.umbc.mahbub1.medialogger.common.utils.DataTransferUtils;
import edu.csee.umbc.mahbub1.medialogger.common.utils.MessagePath;
import io.realm.Realm;


/**
 * Created by mahbub on 1/24/17.
 */

public class AndroidWearClient {

    private static final String TAG = AndroidWearClient.class.getSimpleName();

    public static final long UPDATE_INTERVAL_DEFAULT = TimeUnit.SECONDS.toMillis(1);
    public static final long RECENTLY_DURATION = TimeUnit.SECONDS.toMillis(30);
    public String SOURCE_NODE_ID = Secure.ANDROID_ID;
    public static final int MIN_BATCH_SIZE_TO_WRITE_DB = 200;

    public static AndroidWearClient instance;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private ExecutorService mExecutorService;
    private ExecutorService mLocalThreadPoolExecutor;
    private int filterSensorId;
    private SparseLongArray mLastSensorData;
    private SparseLongArray mLastBatchSendData;
    private Map<Integer, DataBatch> mCurrentDataBatches;

    private List<Data> mSensorDataToStore;
    private int UpdateInterValRandom;
    private String androidDeviceName;
    private boolean isStorageLocal;
    private WearApp wearAppInstance;
    private long numberofdataStored=0;
    //private Resources resources;

    // singleton class to get a static instance of the current wear instance
    public static AndroidWearClient getInstance(Context context) {

        if (instance == null) {
            instance = new AndroidWearClient(context.getApplicationContext());
        }
        return instance;
    }

    //private
    private AndroidWearClient(Context context) {
        this.mContext = context;


        mExecutorService = Executors.newCachedThreadPool(); //create new multiple thread and executer can take any one of them
        mLocalThreadPoolExecutor = Executors.newCachedThreadPool();
        mLastSensorData = new SparseLongArray();
        mLastBatchSendData = new SparseLongArray();
        mCurrentDataBatches = new HashMap<>();
        mSensorDataToStore = new ArrayList<>();
        this.SOURCE_NODE_ID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        UpdateInterValRandom = (1 + new Random().nextInt(6));
        androidDeviceName = Build.MANUFACTURER + Build.SERIAL.substring(Build.SERIAL.length() - 4) + " up freq=" + UpdateInterValRandom + " s";
        //this.wearAppInstance=WearApp.getInstance();
        this.isStorageLocal = false;
        wearAppInstance = WearApp.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
    }


    public void setSensorToFilter(int filterSensorId) {
        this.filterSensorId = filterSensorId;
    }


    public void sendSensorData(boolean storageLocation, Map<Integer, DataBatch> dataBatches, final int sensorType) {
        //public void sendSensorData(final int sensorType, final int accuracy, final long timestamp, final float[] values){

        this.isStorageLocal = storageLocation;
        long currentTimeStamp = System.currentTimeMillis();

        long lastTimeStamp = mLastSensorData.get(sensorType);
        long timeGap = currentTimeStamp - lastTimeStamp;
        this.mCurrentDataBatches = dataBatches;

        long randomUpdateTime = TimeUnit.SECONDS.toMillis(UpdateInterValRandom);
        //Log.d(TAG, "Update Interval " + randomUpdateTime + " milliseconds ");


        if (timeGap < randomUpdateTime)
            return;

        final String jsonResponse = generateDataRequestResponse().toJson();
        Log.d(TAG,jsonResponse);

        mLastSensorData.put(sensorType, currentTimeStamp);

        Log.d(TAG, "sending data....to mobile");

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                storeOrTransferDataInBackGround(sensorType,  jsonResponse);
            }
        });


    }



    public void sendExistingSensorDataBeforeStop(boolean storageLocation, Map<Integer, DataBatch> dataBatches, final int sensorType) {
        //public void sendSensorData(final int sensorType, final int accuracy, final long timestamp, final float[] values){

        this.isStorageLocal = storageLocation;
        long currentTimeStamp = System.currentTimeMillis();

        long lastTimeStamp = mLastSensorData.get(sensorType);
        long timeGap = currentTimeStamp - lastTimeStamp;
        this.mCurrentDataBatches = dataBatches;

        long randomUpdateTime = TimeUnit.SECONDS.toMillis(UpdateInterValRandom);
        Log.d(TAG, "Update Interval " + randomUpdateTime + " milliseconds ");



        final String jsonResponse = generateDataRequestResponse().toJson();
       // Logger.d(this.mContext,TAG,jsonResponse);
        mLastSensorData.put(sensorType, currentTimeStamp);
        Log.d(TAG, "sending remaining data ....");
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                storeOrTransferDataInBackGround(sensorType, jsonResponse);
            }
        });


    }

    public void storeOrTransferDataInBackGround(int sensorType,  String jsonResponse) {
        if (validateConnectionWithHost() && !this.isStorageLocal) {
            sendSensorDataToHostInBackGround(sensorType,  jsonResponse);
        } else {
            storeToLocalWatchInBackground(sensorType,  jsonResponse);
        }
    }

    public void storeToLocalWatchInBackground(int sensorType, final String jsonResponse) {
        Thread t = Thread.currentThread();
        Log.d(TAG, "Thread in executable mode in watch, thread name: " + t.getName());

/*
        Gson gson = new Gson();
        DataRequestResponse response = DataRequestResponse.fromJson(jsonResponse);
        //final String sourceNodeId = putDataRequest.getString(DataMapFields.KEY_SOURCE_NODE_ID);
*/
        /*
         * add all databatches to mSensorDataToStore and check if it more than 200 or not.
         * if more than 200 threshold store it to database as a column
         */
/*
        for (DataBatch dataBatch : response.getDataBatches()) {

            mSensorDataToStore.addAll(dataBatch.getDataList());
            Log.d(TAG, "batches datasize:"+dataBatch.getDataList().size());
        }


        Log.d(TAG, "collection size"+mSensorDataToStore.size());
            if (mSensorDataToStore.size() > MIN_BATCH_SIZE_TO_WRITE_DB) { //check number of data but store as each batch

                Log.d(TAG, mSensorDataToStore.size() + " will be stored this time");
                Log.d(TAG, "oldest:" + mSensorDataToStore.get(0).getTimestamp() + " newest " + mSensorDataToStore.get(mSensorDataToStore.size() - 1).getTimestamp());
                this.numberofdataStored += mSensorDataToStore.size();
                Log.d(TAG, "pro_sto_data total data stored: " + this.numberofdataStored);

                //final List<Data> dataToWrite = new ArrayList<>(mSensorDataToStore);


                Type listOfTestObject = new TypeToken<List<Data>>() {
                }.getType();
                // final String s = gson.toJson(dataBatch.getDataList(), listOfTestObject);

                final String s = gson.toJson(mSensorDataToStore, listOfTestObject);
                /**
                 * as this will be stored, create new instance of mSensorDataToStore
                 */
/*
                mSensorDataToStore = new ArrayList<>();
                //this need to check some null for mSensorDataToStore, so better remove this later

*/
                new Thread() {
                    @Override
                    public void run() {
                        WearApp.getInstance();
                        // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                        final Realm realm = Realm.getDefaultInstance();
                        Log.d(TAG, realm.getConfiguration().toString());
                        realm.beginTransaction();

                        //realm.executeTransaction(new Realm.Transaction() {
                        //    @Override
                        //    public void execute(Realm realm) {
                        AtomicLong productPrimaryKey = new AtomicLong(realm.where(WearableSensorDataList.class).max("id") == null ? 0 : realm.where(WearableSensorDataList.class).max("id").longValue() + 1);

                        long id = productPrimaryKey.getAndIncrement();
                        WearableSensorDataList entry = realm.createObject(WearableSensorDataList.class, id);

                        entry.setJsonAsString(jsonResponse);
                        entry.setStatus(DataTransferUtils.STATUS_DATA_TRANSFER_INCOMPLETE);
                        entry.setAndroidDevice(SOURCE_NODE_ID);
                        Log.d(TAG, "source id: " + SOURCE_NODE_ID);
                        //   }

                        realm.commitTransaction();
                        realm.close();
                        //Thread t = Thread.currentThread();
                        //Log.d(TAG, "tname realm:"+t.getName()+"datasize: "+dataToWrite.size());
                        //});


                    }
                }.start();

                //mLocalThreadPoolExecutor.execute(newTask);
           /* }else {
                Log.d(TAG, mSensorDataToStore.size() + " is less than = " + MIN_BATCH_SIZE_TO_WRITE_DB);
            }*/


    }


    public void sendSensorDataToHostInBackGround(int sensorType,  String jsonResponse) {
        Thread t = Thread.currentThread();
        Log.d(TAG, "Thread in executable mode to remote phone, thread name: " + t.getName());


        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(MessagePath.PATH_WEARABLE_DATA + sensorType);//"/wearesensors/"+sensorType);


        Log.d(TAG, "datasize  " + jsonResponse.getBytes(DataTransferUtils.DEFAULT_CHARSET).length + " bytes");
        dataMapRequest.getDataMap().putByteArray(DataMapFields.RESPONSE, jsonResponse.getBytes(DataTransferUtils.DEFAULT_CHARSET));
        dataMapRequest.getDataMap().putString(DataMapFields.KEY_SOURCE_NODE_ID, SOURCE_NODE_ID);
        dataMapRequest.getDataMap().putString(DataMapFields.DEVICE_NAME, androidDeviceName);


        PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();

        Log.d(TAG, "data_transfer " + jsonResponse);
        sendToHost(putDataRequest);

    }

    /*private boolean validateConnectionWithHost() {
        if (wearAppInstance.getmGoogleApiClient().isConnected()) {
            return true;
        }

        ConnectionResult connectionResult = wearAppInstance.getmGoogleApiClient().blockingConnect(wearAppInstance.getClientConnectionTimeout(), TimeUnit.MILLISECONDS);
        Log.d(TAG,"connection status"+connectionResult.isSuccess());
        return connectionResult.isSuccess();
    }*/


    private void sendToHost(PutDataRequest putDataRequest) {
        Log.d(TAG, "sending to host as storage to local is : " + this.isStorageLocal);

        //if (validateConnectionWithHost() && !this.isStorageLocal) {
            Log.d(TAG, "connected and sending data to mobile phone");
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "buildWatchOnlyNotification(): Failed to set the data, "
                                        + "status: " + dataItemResult.getStatus().getStatusCode());
                            }
                            else{
                                Log.d(TAG, "successfully set data");
                            }
                        }
                    });
        }
    //}


    private DataRequestResponse generateDataRequestResponse() {
        // get all required data batches


        long lastEndTimestamp = mLastBatchSendData.get(Sensor.TYPE_ACCELEROMETER); // any sensors last time data
        long datasize=0;
        List<DataBatch> dataBatches = new ArrayList<>();
        //for (int i=0;i<2;i++) {
        DataBatch existingDataBatch = mCurrentDataBatches.get(Sensor.TYPE_ACCELEROMETER);
        if (existingDataBatch == null) {
            //continue;
        } else {

            DataBatch dataBatch = new DataBatch(existingDataBatch);

            // trim batch to only contain the new data
            //long lastEndTimestamp = mLastBatchSendData.get(Sensor.TYPE_ACCELEROMETER);
            dataBatch.setDataList(dataBatch.getDataSince(lastEndTimestamp));
            dataBatches.add(dataBatch);
            datasize+=dataBatch.getDataList().size();
        }

        existingDataBatch = mCurrentDataBatches.get(Sensor.TYPE_GYROSCOPE);
        if (existingDataBatch == null) {
            //continue;
        } else {

            DataBatch dataBatch = new DataBatch(existingDataBatch);

            // trim batch to only contain the new data
            //long lastEndTimestamp = mLastBatchSendData.get(Sensor.TYPE_GYROSCOPE);
            dataBatch.setDataList(dataBatch.getDataSince(lastEndTimestamp));
            dataBatches.add(dataBatch);
            datasize+=dataBatch.getDataList().size();
        }
        // }

        // create response object
        DataRequestResponse dataRequestResponse = new DataRequestResponse(dataBatches);
        dataRequestResponse.setStartTimestamp(lastEndTimestamp);
        dataRequestResponse.setEndTimestamp(System.currentTimeMillis());
        dataRequestResponse.setDataSize(datasize);

        // update last used timestamp
        lastEndTimestamp = System.currentTimeMillis();
        mLastBatchSendData.put(Sensor.TYPE_ACCELEROMETER, lastEndTimestamp);
        return dataRequestResponse;
    }


    public boolean validateConnectionWithHost() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(wearAppInstance.getClientConnectionTimeout(), TimeUnit.MILLISECONDS);
        return connectionResult.isSuccess();
    }


    public void StartAndEndTimeStamp(){


    }
}
