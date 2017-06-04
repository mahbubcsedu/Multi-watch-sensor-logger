/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.csee.umbc.mahbub1.medialogger.common.recording;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.csee.umbc.mahbub1.medialogger.common.R;
import edu.csee.umbc.mahbub1.medialogger.common.TransferUtils;
import edu.csee.umbc.mahbub1.medialogger.common.database.DBHelper;

//import com.example.android.wearable.medialogger.common.R;

/**
 * A helper class to provide methods to record audio input from the MIC to the internal storage
 * and to playback the same recorded audio file.
 */
public class SoundRecorder {

    private static final String TAG = SoundRecorder.class.getSimpleName();
    private static final int RECORDING_RATE = 8000; // can go up to 44K, if needed
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static int BUFFER_SIZE = AudioRecord
            .getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT);

    private final String mOutputFileName;
    private final AudioManager mAudioManager;
    private final Handler mHandler;
    private final Context mContext;
    private State mState = State.IDLE;

    private OnVoicePlaybackStateChangedListener mListener;
    private AsyncTask<Void, Void, Void> mRecordingAsyncTask;
    private AsyncTask<Void, Void, Void> mPlayingAsyncTask;

    private static String mFileName = null;
    private String mFilePath = null;
    private DBHelper mDatabase;
    Resources res;
    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    enum State {
        IDLE, RECORDING, PLAYING
    }

    public SoundRecorder(Context context, String outputFileName,
                         OnVoicePlaybackStateChangedListener listener) {
        mOutputFileName = outputFileName;
        mListener = listener;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mContext = context;
        res = context.getResources();
        mDatabase = new DBHelper(context.getApplicationContext());
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    /**
     * Starts recording from the MIC.
     */
    public void startRecording() {
        if (mState != State.IDLE) {
            Log.w(TAG, "Requesting to start recording while state was not IDLE");
            return;
        }
        //create soutput file
        setFileNameAndPath();
        //createFileNamePath();

        mRecordingAsyncTask = new AsyncTask<Void, Void, Void>() {

            private AudioRecord mAudioRecord;

            @Override
            protected void onPreExecute() {
                mState = State.RECORDING;
            }

            @Override
            protected Void doInBackground(Void... params) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDING_RATE, CHANNEL_IN, FORMAT, BUFFER_SIZE * 3);
                BufferedOutputStream bufferedOutputStream = null;
                try {
                    bufferedOutputStream = new BufferedOutputStream(//new FileOutputStream(mFilePath));
                    mContext.openFileOutput(mFileName, Context.MODE_PRIVATE));
                    byte[] buffer = new byte[BUFFER_SIZE];
                    mAudioRecord.startRecording();
                    mStartingTimeMillis = System.currentTimeMillis();
                    while (!isCancelled()) {
                        int read = mAudioRecord.read(buffer, 0, buffer.length);
                        bufferedOutputStream.write(buffer, 0, read);
                        Log.d(TAG, "recording...");
                    }
                } catch (IOException | NullPointerException | IndexOutOfBoundsException e) {
                    Log.e(TAG, "Failed to record data: " + e);
                } finally {
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    mAudioRecord.release();
                    mAudioRecord = null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mState = State.IDLE;
                mRecordingAsyncTask = null;
            }

            @Override
            protected void onCancelled() {
                if (mState == State.RECORDING) {
                    Log.d(TAG, "Stopping the recording ...");
                    mState = State.IDLE;

                } else {
                    Log.w(TAG, "Requesting to stop recording while state was not RECORDING");
                }
                mRecordingAsyncTask = null;
            }
        };

        mRecordingAsyncTask.execute();
    }

    public void stopRecording() {
        if (mRecordingAsyncTask != null) {
            mRecordingAsyncTask.cancel(true);
        }

        //mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        //mRecorder.release();
        //Toast.makeText(this, res.getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        //mRecorder = null;

        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                //NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                //mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    public void stopPlaying() {
        if (mPlayingAsyncTask != null) {
            mPlayingAsyncTask.cancel(true);
        }
    }

    /**
     * Starts playback of the recorded audio file.
     */
    public void startPlay() {
        if (mState != State.IDLE) {
            Log.w(TAG, "Requesting to play while state was not IDLE");
            return;
        }

        //if (!new File(mContext.getFilesDir(), mOutputFileName).exists()) {
        //if (!new File(mOutputFileName).exists()) {
        if (!new File(TransferUtils.getAlbumStorageDir("cliprecorder"), mOutputFileName).exists()) {
            // there is no recording to play
            if (mListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onPlaybackStopped();
                    }
                });
            }
            return;
        }
        final int intSize = AudioTrack.getMinBufferSize(RECORDING_RATE, CHANNELS_OUT, FORMAT);

        mPlayingAsyncTask = new AsyncTask<Void, Void, Void>() {

            private AudioTrack mAudioTrack;

            @Override
            protected void onPreExecute() {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0 /* flags */);
                mState = State.PLAYING;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDING_RATE,
                            CHANNELS_OUT, FORMAT, intSize, AudioTrack.MODE_STREAM);
                    byte[] buffer = new byte[intSize * 2];
                    FileInputStream in = null;
                    BufferedInputStream bis = null;
                    mAudioTrack.setVolume(AudioTrack.getMaxVolume());
                    mAudioTrack.play();
                    try {
                        in = mContext.openFileInput(mOutputFileName);
                        bis = new BufferedInputStream(in);
                        int read;
                        while (!isCancelled() && (read = bis.read(buffer, 0, buffer.length)) > 0) {
                            mAudioTrack.write(buffer, 0, read);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read the sound file into a byte array", e);
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                            if (bis != null) {
                                bis.close();
                            }
                        } catch (IOException e) { /* ignore */}

                        mAudioTrack.release();
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to start playback", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                cleanup();
            }

            @Override
            protected void onCancelled() {
                cleanup();
            }

            private void cleanup() {
                if (mListener != null) {
                    mListener.onPlaybackStopped();
                }
                mState = State.IDLE;
                mPlayingAsyncTask = null;
            }
        };

        mPlayingAsyncTask.execute();
    }

    public interface OnVoicePlaybackStateChangedListener {

        /**
         * Called when the playback of the audio file ends. This should be called on the UI thread.
         */
        void onPlaybackStopped();
    }

    /**
     * Cleans up some resources related to {@link AudioTrack} and {@link AudioRecord}
     */
    public void cleanup() {
        Log.d(TAG, "cleanup() is called");
        stopPlaying();
        stopRecording();
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    private void createFileNamePath() {
        boolean created = false;
        int count = 0;
        File f;


        if (!isExternalStorageWritable()) {
            Log.d(TAG, "External Storage Not Writable");
            return;
        }


        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/soundstorage");
            created = directory.mkdirs();
            Log.d(TAG, "Directory created ? " + created);


            do {
                count++;

                mFileName = res.getString(R.string.default_file_name)
                        + " #" + (mDatabase.getCount() + count) + ".mp4";
                mFilePath = directory.getAbsolutePath() +"/"+ mFileName;
                //mFilePath += "/SoundRecorder/" + mFileName;
                //mOutputFileName = mFilePath;

                f = new File(mFilePath);
            } while (f.exists() && !f.isDirectory());
        } catch (Exception e) {
            e.printStackTrace();
        }


        // File file = new File(directory, "wearable_data.txt");

    }


    public void setFileNameAndPath() {


        int count = 0;
        File f;

        do {
            count++;

            mFileName = res.getString(R.string.default_file_name)
                    + "_" + (mDatabase.getCount() + count) + ".pcm";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;
            //mOutputFileName = mFilePath;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }


}
