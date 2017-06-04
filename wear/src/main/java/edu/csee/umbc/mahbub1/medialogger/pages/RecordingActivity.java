package edu.csee.umbc.mahbub1.medialogger.pages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;

import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.common.Constants;
import edu.csee.umbc.mahbub1.medialogger.common.data.PreferenceData;
import edu.csee.umbc.mahbub1.medialogger.common.recording.SoundRecorder;
import edu.csee.umbc.mahbub1.medialogger.common.utils.MessagePath;
import edu.csee.umbc.mahbub1.medialogger.sensor.SensorServices;

import static edu.csee.umbc.mahbub1.medialogger.common.TransferUtils.getDataFromMessageAsString;

public class RecordingActivity extends WearableActivity implements SoundRecorder.OnVoicePlaybackStateChangedListener {
    public static final String TAG = RecordingActivity.class.getSimpleName();

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private CircledImageView mRecordButton = null;
    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private ProgressBar mProgressBar;
    private Handler mHandler;



    private static final int PERMISSIONS_REQUEST_CODE = 100;
   // private static final long COUNT_DOWN_MS = TimeUnit.SECONDS.toMillis(10);
    //private static final long MILLIS_IN_SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final String VOICE_FILE_NAME = "audiorecord.pcm";
    private MediaPlayer mMediaPlayer;
    private AppState mState = AppState.READY;
    private SoundRecorder mSoundRecorder;


    private static String mFileName = null;

    enum AppState {
        READY, PLAYING_VOICE, PLAYING_MUSIC, RECORDING
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_activity);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        //mTextView = (TextView) findViewById(R.id.text);
        //mClockView = (TextView) findViewById(R.id.clock);

        //setContentView(R.layout.activity_main);
        mRecordButton = (CircledImageView) findViewById(R.id.btn_record);
        mState = AppState.READY;
        //mUiState = state;
        //mSoundRecorder.startRecording();


        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        // ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


    mRecordButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            CircledImageView view = (CircledImageView)v;
            //boolean mStartRecording = true;
            //onRecord(mStartRecording);
            if (mState== AppState.READY) {
                Log.d(TAG, "appstead Ready, start recording");
                view.setImageResource(R.drawable.ic_stop_black_120dp);
                mState = AppState.RECORDING;
                start();
                mSoundRecorder.startRecording();
                //setText("Stop recording");
            } else  {
                view.setImageResource(R.drawable.ic_mic_120dp);

                mState = AppState.READY;
                mSoundRecorder.stopRecording();
            }
            //mStartRecording = !mStartRecording;
        }


    });

        mWearManager = WearManager.getInstance();
        // We register a listener to be notified when messages arrive while we are on this page.
        // We then filter messages based on their path to identify the ones that report on the
        // navigation in the companion phone app. When such message is discovered, we write the name
        // of new page to the view.
        mWearConsumer = new AbstractWearConsumer() {
            @Override
            public void onWearableMessageReceived(MessageEvent messageEvent) {
                Log.d(TAG, "Received Message: "+messageEvent.getPath());
                Intent intent = new Intent(getApplicationContext(),SensorServices.class);

//TODO: have to implement the sensor information here.

                if(messageEvent.getPath().equals(MessagePath.START_MEASUREMENT)){//getApplicationContext().getResources().getString(R.string.START_RECORDING_PATH))){

                    String prefData=getDataFromMessageAsString(messageEvent.getData());
                    PreferenceData preferenceData=PreferenceData.fromJson(prefData);
                    preferenceData.getSensor_frequency();
                    preferenceData.isStorageLocationIsWatch();
                    intent.putExtra("isStorageLocal",preferenceData.isStorageLocationIsWatch());
                    startService(intent);
                }
                if(messageEvent.getPath().equals(MessagePath.STOP_MEASUREMENT)){//getApplicationContext().getResources().getString(R.string.STOP_RECORDING_PATH))){
                    stopService(intent);
                }

                if (Constants.NAVIGATION_PATH_MOBILE.equals(messageEvent.getPath())) {
                    DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
                    int currentPage  = dataMap.getInt(Constants.KEY_PAGE, Constants.TARGET_INTRO);

                }
                  //writeMessage(currentPage);
            }
        };

}

   /**
     * Checks the permission that this app needs and if it has not been granted, it will
     * prompt the user to grant it, otherwise it shuts down the app.
     */
    private void checkPermissions() {
        boolean recordAudioPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;

        if (recordAudioPermissionGranted) {
            start();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                // Permission has been denied before. At this point we should show a dialog to
                // user and explain why this permission is needed and direct him to go to the
                // Permissions settings for the app in the System settings. For this sample, we
                // simply exit to get to the important part.
                Toast.makeText(this, R.string.exiting_for_permissions, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /**
     * Starts the main flow of the application.
     */
    private void start() {
        mSoundRecorder = new SoundRecorder(this, VOICE_FILE_NAME, this);
        /*int[] thumbResources = new int[] {R.id.mic, R.id.play, R.id.music};
        ImageView[] thumbs = new ImageView[3];
        for(int i=0; i < 3; i++) {
            thumbs[i] = (ImageView) findViewById(thumbResources[i]);
        }
        View containerView = findViewById(R.id.container);
        ImageView expandedView = (ImageView) findViewById(R.id.expanded);
        int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mUIAnimation = new UIAnimation(containerView, thumbs, expandedView, animationDuration,
                this);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (speakerIsSupported()) {
            checkPermissions();
        } else {
            //findViewById(R.id.container2).setOnClickListener(new View.OnClickListener() {
              //  @Override
               // public void onClick(View v) {
                    Toast.makeText(RecordingActivity.this, R.string.no_speaker_supported,
                            Toast.LENGTH_SHORT).show();
                //}
            //});
        }
    }

    @Override
    protected void onStop() {
        if (mSoundRecorder != null) {
            mSoundRecorder.cleanup();
            mSoundRecorder = null;
        }


        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onStop();
    }

    @Override
    public void onPlaybackStopped() {
        // mUIAnimation.transitionToHome();
        //mUiState = UIAnimation.UIState.HOME;
        mState = AppState.READY;
    }

    /**
     * Determines if the wear device has a built-in speaker and if it is supported. Speaker, even if
     * physically present, is only supported in Android M+ on a wear device..
     */
    public final boolean speakerIsSupported() {
        return true;
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager packageManager = getPackageManager();
            // The results from AudioManager.getDevices can't be trusted unless the device
            // advertises FEATURE_AUDIO_OUTPUT.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false;
            }
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true;
                }
            }
        }
        return false;*/
    }



    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            //mTextView.setTextColor(getResources().getColor(android.R.color.white));
            //mClockView.setVisibility(View.VISIBLE);

            //mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
           // mTextView.setTextColor(getResources().getColor(android.R.color.black));
            //mClockView.setVisibility(View.GONE);
        }
    }
}
