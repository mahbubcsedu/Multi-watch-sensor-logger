package edu.csee.umbc.mahbub1.medialogger.sensor;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.wearable.BuildConfig;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import edu.csee.umbc.mahbub1.medialogger.MyListActivity;
import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.common.data.Data;
import edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch;


/**
 * Created by mahbub on 1/24/17.
 */

public class SensorServices extends Service implements SensorEventListener {
    private static final String TAG = SensorServices.class.getSimpleName(); // set tag for this service


    private Map<Integer, edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch> sensorDataBatches;
    private edu.csee.umbc.mahbub1.medialogger.common.sensors.SensorNames mSensorNames;

    // 2 general sensor usually embeded on all system
    private final static int SENSOR_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int SENSOR_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
    //  3 @Deprecated Orientation sensors
    private final static int SENSOR_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    private final static int SENSOR_LIGHT = Sensor.TYPE_LIGHT;
    private final static int SENSOR_PRESSURE = Sensor.TYPE_PRESSURE;
    // 7 @Deprecated temperature sensors
    private final static int SENSOR_PROXIMITY = Sensor.TYPE_PROXIMITY;
    private final static int SENSOR_GRAVITY = Sensor.TYPE_GRAVITY;
    private final static int SENSOR_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
    private final static int SENSOR_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
    private final static int SENSOR_RELATIVE_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY;
    // TODO: these sensoror may be included in future
    private final static int SENSOR_AMBIENT_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE;
    private final static int SENSOR_MAGNETIC_FIELD_UNCALLIBRATED = Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
    private final static int SENSOR_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;
    private final static int SENSOR_GYROSCOPE_UNCALIBRATED = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
    private final static int SENSOR_SIGNIFICANT_MOTION = Sensor.TYPE_SIGNIFICANT_MOTION;
    private final static int SENSOR_STEP_DETECTOR = Sensor.TYPE_STEP_DETECTOR;
    private final static int SENSOR_STEP_COUNTER = Sensor.TYPE_STEP_COUNTER;
    private final static int SENSOR_GEOMAGNETIC_ROTATION_VECTOR = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
    private final static int SENSOR_HEARTRATE = Sensor.TYPE_HEART_RATE;

    // get instance of sensor manager to get sensor services
    SensorManager mSensorManager;

    private Sensor mHeartrateSensor;



    private AndroidWearClient mAndroidWearClient;
    private ScheduledExecutorService mScheduler;

    private NotificationManager mNM;
    private long[] numberOfData= {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    //private int NOTIFICATION = R.string.local_service_started;
    private edu.csee.umbc.mahbub1.medialogger.common.status.StorageStatus storageStatus;
    private boolean isStoreToLocal = false;
    private int sensorFrequency;
    private static final int NOTIFICATION_ID = 1;
    private static final String GROUP_KEY_MESSAGES = "messages";



    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
        mAndroidWearClient = AndroidWearClient.getInstance(this);
        Log.i(TAG, "Sensor remote connected");
        /*Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Streaming..");
        builder.setContentText("Collecting sensor data..");
        builder.setSmallIcon(R.mipmap.ic_launcher);
*/


        PendingIntent conversationPendingIntent = getConversationPendingIntent("Preppy Rabbit", 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setContentTitle("Streaming..")
                .setContentText("Swip right to change")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(conversationPendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);
                //.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFICATION_ID, notification.build());



       // startForeground(1, builder.build());






        keepCPUAwake();

        startSensorDataRecording();
        storageStatus = new edu.csee.umbc.mahbub1.medialogger.common.status.StorageStatus();
        this.sensorFrequency = SensorManager.SENSOR_DELAY_NORMAL;


        //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        //showNotification();
    }



    private PendingIntent getConversationPendingIntent(String chattingWith, int requestCode) {
        Intent conversationIntent = new Intent(this, MyListActivity.class);

        if(chattingWith != null) {
            //conversationIntent.putExtra(ListViewActivity.EXTRA_CHATTING_WITH, chattingWith);
        }

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MyListActivity.class);
        taskStackBuilder.addNextIntent(conversationIntent);

        return taskStackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    @SuppressLint("LongLogTag")
    private void initializeSensorDataBatches() {
        sensorDataBatches = new HashMap<>();
        //List<Sensor> availableSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //for (Sensor sensor : availableSensors) {
        sensorDataBatches.put(Sensor.TYPE_ACCELEROMETER, getDataBatch(Sensor.TYPE_ACCELEROMETER));
        sensorDataBatches.put(Sensor.TYPE_GYROSCOPE, getDataBatch(Sensor.TYPE_GYROSCOPE));


        Log.d(TAG, "sensor object created");
        //}
    }

    private void keepCPUAwake() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG);
        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            String source = null == intent ? "intent" : "action";
            Log.e(TAG, source + " was null, flags=" + flags + " bits=" + Integer.toBinaryString(flags));
        } else {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.isStoreToLocal = (boolean) extras.getBoolean("isStorageLocal");
                this.storageStatus.setStorageOnLocalWatch(this.isStoreToLocal);
                this.sensorFrequency = (int) extras.getInt("frequency",SensorManager.SENSOR_DELAY_GAME);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "Destroy");
        //mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        //Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
        stopSensorDataRecording();
    }


    @SuppressLint("LongLogTag")
    protected void startSensorDataRecording() {
        Log.d(TAG, "start data recording");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorNames = new edu.csee.umbc.mahbub1.medialogger.common.sensors.SensorNames();
        initializeSensorDataBatches();

        //this.storageStatus.setFinishedUnsentData(false);

        if (BuildConfig.DEBUG) {
            logAvailableSensors();
        }

        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENSOR_ACCELEROMETER);
       /* Sensor ambientTemperatureSensor = mSensorManager.getDefaultSensor(SENSOR_AMBIENT_TEMPERATURE);
        Sensor gameRotationVectorSensor = mSensorManager.getDefaultSensor(SENSOR_GAME_ROTATION_VECTOR);
        Sensor geomagneticSensor = mSensorManager.getDefaultSensor(SENSOR_GEOMAGNETIC_ROTATION_VECTOR);
        Sensor gravitySensor = mSensorManager.getDefaultSensor(SENSOR_GRAVITY);
        */
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENSOR_GYROSCOPE);
        /*Sensor gyroscopeUncalibratedSensor = mSensorManager.getDefaultSensor(SENSOR_GYROSCOPE_UNCALIBRATED);
        mHeartrateSensor = mSensorManager.getDefaultSensor(SENSOR_HEARTRATE);
        Sensor heartrateSamsungSensor = mSensorManager.getDefaultSensor(65562);
        Sensor lightSensor = mSensorManager.getDefaultSensor(SENSOR_LIGHT);
        Sensor linearAccelerationSensor = mSensorManager.getDefaultSensor(SENSOR_LINEAR_ACCELERATION);
        Sensor magneticFieldSensor = mSensorManager.getDefaultSensor(SENSOR_MAGNETIC_FIELD);
        Sensor magneticFieldUncalibratedSensor = mSensorManager.getDefaultSensor(SENSOR_MAGNETIC_FIELD_UNCALLIBRATED);
        Sensor pressureSensor = mSensorManager.getDefaultSensor(SENSOR_PRESSURE);
        Sensor proximitySensor = mSensorManager.getDefaultSensor(SENSOR_PROXIMITY);
        Sensor humiditySensor = mSensorManager.getDefaultSensor(SENSOR_RELATIVE_HUMIDITY);
        Sensor rotationVectorSensor = mSensorManager.getDefaultSensor(SENSOR_ROTATION_VECTOR);
        Sensor significantMotionSensor = mSensorManager.getDefaultSensor(SENSOR_SIGNIFICANT_MOTION);
        Sensor stepCounterSensor = mSensorManager.getDefaultSensor(SENSOR_STEP_COUNTER);
        Sensor stepDetectorSensor = mSensorManager.getDefaultSensor(SENSOR_STEP_DETECTOR);
        */


        // Register the listener
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {


                //mSensorManager.registerListener(this, accelerometerSensor, this.sensorFrequency);//SensorManager.SENSOR_DELAY_GAME);//.SENSENSOR_DELAY_FASTEST);
                mSensorManager.registerListener(this, accelerometerSensor, 1000000);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }

           /* if (ambientTemperatureSensor != null) {
                mSensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "Ambient Temperature Sensor not found");
            }

            if (gameRotationVectorSensor != null) {
                mSensorManager.registerListener(this, gameRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "Gaming Rotation Vector Sensor not found");
            }

            if (geomagneticSensor != null) {
                mSensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Geomagnetic Sensor found");
            }

            if (gravitySensor != null) {
                mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Gravity Sensor");
            }*/

            if (gyroscopeSensor != null) {
                //mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);//.SENSOR_DELAY_FASTEST);
               // mSensorManager.registerListener(this,gyroscopeSensor,this.sensorFrequency);
                mSensorManager.registerListener(this, gyroscopeSensor, 1000000);
            } else {
                Log.w(TAG, "No Gyroscope Sensor found");
            }
            /*

            if (gyroscopeUncalibratedSensor != null) {
                mSensorManager.registerListener(this, gyroscopeUncalibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Uncalibrated Gyroscope Sensor found");
            }

            if (mHeartrateSensor != null) {
                final int measurementDuration   = 30;   // Seconds
                final int measurementBreak      = 15;    // Seconds

                mScheduler = Executors.newScheduledThreadPool(1);
                mScheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(SensorServices.this, mHeartrateSensor, SensorManager.SENSOR_DELAY_FASTEST);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorServices.this, mHeartrateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);

            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }

            if (heartrateSamsungSensor != null) {
                mSensorManager.registerListener(this, heartrateSamsungSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.d(TAG, "Samsungs Heartrate Sensor not found");
            }

            if (lightSensor != null) {
                mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Light Sensor found");
            }

            if (linearAccelerationSensor != null) {
                mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Linear Acceleration Sensor found");
            }

            if (magneticFieldSensor != null) {
                mSensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Magnetic Field Sensor found");
            }

            if (magneticFieldUncalibratedSensor != null) {
                mSensorManager.registerListener(this, magneticFieldUncalibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No uncalibrated Magnetic Field Sensor found");
            }

            if (pressureSensor != null) {
                mSensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Pressure Sensor found");
            }

            if (proximitySensor != null) {
                mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Proximity Sensor found");
            }

            if (humiditySensor != null) {
                mSensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Humidity Sensor found");
            }

            if (rotationVectorSensor != null) {
                mSensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Rotation Vector Sensor found");
            }

            if (significantMotionSensor != null) {
                mSensorManager.registerListener(this, significantMotionSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Significant Motion Sensor found");
            }

            if (stepCounterSensor != null) {
                mSensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Step Counter Sensor found");
            }

            if (stepDetectorSensor != null) {
                mSensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Step Detector Sensor found");
            }
            */
        }
    }



       /* Sensor accelerometerSensor = mSensorManager.getDefaultSensor(SENSOR_ACCELEROMETER);
        Sensor gyroscopeSensor = mSensorManager.getDefaultSensor(SENSOR_GYROSCOPE);
        Sensor gravitySensor = mSensorManager.getDefaultSensor(SENSOR_GRAVITY);
        Sensor geometricSensor = mSensorManager.getDefaultSensor(SENSOR_GEOMAGNETIC_ROTATION_VECTOR);
        Sensor significantMotionSensor = mSensorManager.getDefaultSensor(SENSOR_SIGNIFICANT_MOTION);
        Sensor proximitySensor = mSensorManager.getDefaultSensor(SENSOR_PROXIMITY);
        Sensor rotationVectorSensor = mSensorManager.getDefaultSensor(SENSOR_ROTATION_VECTOR);
        Sensor magneticFieldSensor = mSensorManager.getDefaultSensor(SENSOR_MAGNETIC_FIELD);



        // register the sensor listener first to get any sensor data

        if(mSensorManager!=null){

            if(accelerometerSensor != null){
            mSensorManager.registerListener(this,accelerometerSensor,mSensorManager.SENSOR_DELAY_NORMAL);

            }else {
            Log.w(TAG, "No accelerometer found");
            }

            if(magneticFieldSensor != null){
                mSensorManager.registerListener(this,magneticFieldSensor,mSensorManager.SENSOR_DELAY_NORMAL);
            }else{
                Log.w(TAG, "no magnatometer found");
            }

            if(rotationVectorSensor != null){
                mSensorManager.registerListener(this,rotationVectorSensor,mSensorManager.SENSOR_DELAY_NORMAL);
            }else{
                Log.w(TAG, "no rotationVectorSensor found");
            }
        }
    }*/


    protected void stopSensorDataRecording() {
        Log.d(TAG, "stop data recording");
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mScheduler != null && !mScheduler.isTerminated()) {
            mScheduler.shutdown();
        }
       // this.storageStatus.setFinishedUnsentData(true);
        mAndroidWearClient.sendExistingSensorDataBeforeStop(this.isStoreToLocal,this.sensorDataBatches,Sensor.TYPE_ACCELEROMETER);

    }

    @SuppressLint("LongLogTag")
    @Override

    public void onSensorChanged(SensorEvent sensorEvent) {

        //Log.d(TAG, "new sensor event occured " + sensorEvent.sensor.getType());
        numberOfData[sensorEvent.sensor.getType()]++;


        float[] values = new float[sensorEvent.values.length];
        System.arraycopy(sensorEvent.values, 0, values, 0, sensorEvent.values.length);
        Data data = new Data(sensorEvent.sensor.getName(), values);

        //Log.d(TAG, "data event:" + Arrays.toString(values));

        Log.d(TAG,"timestamp(ms)="+System.currentTimeMillis()+" sensorTimestamp(nano):"+sensorEvent.timestamp +" sensorType="+sensorEvent.sensor.getType());
        Log.d(TAG, "pro_sto_data accelerometer= " + numberOfData[1] +" Gyroscope+"+numberOfData[4]);
        //Log.d(TAG, "data_reporting: timestamp= " + System.currentTimeMillis() + " , sensorType= "+sensorEvent.sensor.getType()+ ", values=" +Arrays.toString(values));
        //Logger.d(this,TAG,"timestamp= " + System.currentTimeMillis() + " , sensorType= "+sensorEvent.sensor.getType()+ ", values=" + Arrays.toString(values));
        data.setAccuracy(sensorEvent.accuracy);
        data.setSensorTimeStampNonoS(sensorEvent.timestamp);
        getDataBatch(sensorEvent.sensor.getType()).addData(data);

        mAndroidWearClient.sendSensorData(this.isStoreToLocal, this.sensorDataBatches, sensorEvent.sensor.getType());
        //mAndroidWearClient.sendSensorData(sensorEvent.sensor.getType(),sensorEvent.accuracy,sensorEvent.timestamp,sensorEvent.values);

    }

    public DataBatch getDataBatch(int sensorType) {
        DataBatch dataBatch = sensorDataBatches.get(sensorType);
        if (dataBatch == null) {
            dataBatch = createDataBatch(sensorType);
            sensorDataBatches.put(sensorType, dataBatch);
        }
        return dataBatch;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("LongLogTag")
    private void logAvailableSensors() {
        final List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
        Log.d(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", "SensorName", "StringType", "Type"));
        for (Sensor sensor : sensors) {
            Log.v(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|", sensor.getName(), sensor.getStringType(), sensor.getType()));
        }

        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
    }


    private DataBatch createDataBatch(int sensorType) {
        //Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
        //if (sensor == null) {
        //    return null;
        //}
        String sensorName = this.mSensorNames.getName(sensorType);
        DataBatch dataBatch = new DataBatch(sensorName);
        dataBatch.setType(sensorType);
        return dataBatch;
    }


    /**
     * Show a notification while this service is running.
     */
   /* private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainWearActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ringphone)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }*/


}
