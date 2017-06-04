package edu.csee.umbc.mahbub1.medialogger.common;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.charset.Charset;



/**
 * Created by mahbub on 4/27/17.
 * <p>
 * Created by mahbub on 2/6/17.
 */
/**
 * Created by mahbub on 2/6/17.
 */

/**
 * Defines app-wide constants and utilities.
 * Data status is maintained for local data sync with server.
 * When the data is inserted or the insertion part is going on, The status is defined as Incomplete or I.
 * When the data is ready for sync because the date is expired or the user submit button is pressed, The data status becomes Ready to Sync (S).
 * When the data is trying to send some data to server over the network, The data transfer may fail or may succeed, but need to keep track of that. The
 * data which was in transition state is represents with the status IN_QUEUE (Q).
 * If data transfer successfully, The status is set as Complete or C
 *
 * @author Mahbubur Rahman
 * @version 1.0
 */
public  class TransferUtils {

    public static final String TAG=TransferUtils.class.getSimpleName();
    /** The Constant STATUS_DATA_TRANSFER_INCOMPLETE. */
    public static final String STATUS_DATA_TRANSFER_INCOMPLETE = "I";

    /** The Constant STATUS_DATA_TRANSFER_READY. */
    public static final String STATUS_DATA_TRANSFER_READY = "S";

    /** The Constant STATUS_DATA_TRANSFER_IN_QUEUE. */
    public static final String STATUS_DATA_TRANSFER_IN_QUEUE = "Q";

    /** The Constant STATUS_DATA_TRANSFER_COMPLETE. */
    public static final String STATUS_DATA_TRANSFER_COMPLETE="C";

    public static final String SERVER_URL = "https://eclipse.umbc.edu/rtv/androidsensor/public/addsensors";

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");


    public TransferUtils(){

    }

    public static String getDataFromMessageAsString(byte[] data) {
        //byte[] data = getDataFromMessage(message);
        if (data != null) {
            return new String(data, DEFAULT_CHARSET);
        }
        return null;
    }


    /* Checks if external storage is available for read and write */
    public static  boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), albumName);

        if(file.canWrite())
            Log.d(TAG, "write permission");
        else if(file.canRead())
            Log.d(TAG, "has read permission");
        else
            Log.d(TAG, "no read and write permission");

        try {
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.e(TAG, "Directory not created");
                }
            }

    }catch(Exception e){
        Log.d(TAG, e.getLocalizedMessage());
    }
        return file;
    }

    public static String setFileNameAndPath(Activity activity) {


        boolean created = false;
        String mFilePath="";
        int count = 0;
        File f;


        if (!isExternalStorageWritable()) {
            Log.d(TAG, "External Storage Not Writable");
            return activity.getFilesDir().toString();
        }


        try {
            File directory = activity.getFilesDir();//+File.separator+"SoundRecorder");/*
           // File directory = new File(activity.getFilesDir()+File.separator+"SoundRecorder");/*
            /*File directory = new File(android.os.Environment.getExternalStorageDirectory()
                    + "/SoundRecorder");*/
            if(!directory.exists()) {
                created = directory.mkdirs();
                Log.d(TAG, "Directory created ? " + created);
            }


            mFilePath = directory.getAbsolutePath();
            //mOutputFileName = mFilePath;

        }catch (Exception e){
            Log.d(TAG, "directory creation failed=?"+created);
        }
        return mFilePath.toString();
    }


    public static File getFileName(String fileName){
        int count = 0;
        File f;
        String mFilePath;
        String mFileName;


            do {
                count++;


                mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                mFilePath += "/cliprecorder/" + fileName;

                f = new File(mFilePath);
            } while (f.exists() && !f.isDirectory());



        return f;
    }
}