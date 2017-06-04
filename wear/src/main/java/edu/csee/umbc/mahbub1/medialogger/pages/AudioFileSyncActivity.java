package edu.csee.umbc.mahbub1.medialogger.pages;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.google.devrel.wcl.connectivity.WearFileTransfer;
import com.google.devrel.wcl.filters.NearbyFilter;
import com.google.devrel.wcl.filters.SingleNodeFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.WearApplication;
import edu.csee.umbc.mahbub1.medialogger.common.Constants;
import edu.csee.umbc.mahbub1.medialogger.common.TransferUtils;
import edu.csee.umbc.mahbub1.medialogger.common.data.AudioData;
import edu.csee.umbc.mahbub1.medialogger.common.data.AudioDataBatch;
import edu.csee.umbc.mahbub1.medialogger.common.database.DBHelper;
import edu.csee.umbc.mahbub1.medialogger.common.database.RecordingItemDb;

import static edu.csee.umbc.mahbub1.medialogger.common.TransferUtils.DEFAULT_CHARSET;

/**
 * Created by mahbub on 4/28/17.
 */

public class AudioFileSyncActivity extends WearableActivity implements WearFileTransfer.OnChannelTransferProgressListener{
    private static final String TAG = "MainActivity";
    private static final int BUFFER_SIZE = 1024;
    private DBHelper dataBase;
    // the name of the text file that is in the assets directory and will be transferred across in
    // the "high-level" approach
    private static final String TEXT_FILE_NAME = "text_file.txt";

    // the resource pointing to the image that we transfer in the "low-level" approach
    private static final int IMAGE_RESOURCE_ID = R.raw.android_wear;

    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private ProgressBar mProgressBar;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // moving a text file from "assets" directory to the internal app directory so we can get
        // a File reference to that for one of the examples below
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                copyFileToPrivateDataIfNeededAndReturn(TEXT_FILE_NAME);
            }
        }).start();*/

        mHandler = new Handler();
        setContentView(R.layout.file_transfer);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            }
        });
        mWearManager = WearManager.getInstance();

        // we define a listener to inform us of the status of the file transfer
        mWearConsumer = new AbstractWearConsumer() {
            @Override
            public void onWearableSendFileResult(int statusCode, String requestId) {
                Log.d(TAG, String.format("Status Code=%d, requestId=%s", statusCode, requestId));
            }
        };
        dataBase = new DBHelper(this);
        setAmbientEnabled();
    }

    public void onClick(View view) {
        // first we try to find at least one nearby connected node
        Set<Node> nodes = mWearManager
                .getNodesForCapability(Constants.CAPABILITY_FILE_PROCESSOR,
                        new SingleNodeFilter(new NearbyFilter()));

        if (nodes != null && !nodes.isEmpty()) {
            Node targetNode = nodes.iterator().next();
            Log.d(TAG, "Targeting node: " + targetNode);

            switch (view.getId()) {
                case R.id.high_level:
                    sendAudioAsset(targetNode);
                    // high-level approach
                    /*WearFileTransfer fileTransferHighLevel = new WearFileTransfer.Builder(
                            targetNode)
                            .setTargetName(TEXT_FILE_NAME)
                            .setFile(copyFileToPrivateDataIfNeededAndReturn(TEXT_FILE_NAME))
                            .build();
                    fileTransferHighLevel.startTransfer();*/
                    break;
                case R.id.low_level:
                    sendAudioAsset(targetNode);
                    // the "low-level" approach
                   /* WearFileTransfer fileTransferLowLevel = new WearFileTransfer.Builder(
                            targetNode)
                            .setOnChannelOutputStreamListener(
                                    new OutputStreamListener(IMAGE_RESOURCE_ID,
                                            AudioFileSyncActivity.this))
                            .build();
                    fileTransferLowLevel.requestOutputStream();*/
                    break;
            }

        } else {
            Toast.makeText(this, R.string.no_node_available, Toast.LENGTH_SHORT).show();
        }
    }


    public void sendAudioAsset(Node targetNode) {

        String WHERE = DBHelper.DBHelperItem.COLUMN_NAME_TRANSFER_STATUS + " = '" + TransferUtils.STATUS_DATA_TRANSFER_INCOMPLETE +"' OR " +
                DBHelper.DBHelperItem.COLUMN_NAME_TRANSFER_STATUS + " = '" +TransferUtils.STATUS_DATA_TRANSFER_IN_QUEUE +"' OR " +
                DBHelper.DBHelperItem.COLUMN_NAME_TRANSFER_STATUS + " = '" + TransferUtils.STATUS_DATA_TRANSFER_READY+"'";


        ArrayList<RecordingItemDb> items = dataBase.getItems(null);

        //Log.d(TAG, items.get(0).getName());


        AudioData audioData = new AudioData();
        AudioDataBatch dataBatch = new AudioDataBatch();

        WearFileTransfer fileTransferHighLevel;
        for(RecordingItemDb item: items){
            audioData = new AudioData();
            audioData.setTimestamp(item.getTime());
            audioData.setLength(item.getLength());
            audioData.setClipName(item.getName());
            audioData.setLocalId(item.getId());
            //audioData.setWearRecordId(item.);
            dataBatch.addData(audioData);

            fileTransferHighLevel = new WearFileTransfer.Builder(
                    targetNode)
                    .setTargetName(item.getName())
                    .setFile(copyFileToPrivateDataIfNeededAndReturn(item.getName()))
                    .build();
            fileTransferHighLevel.startTransfer();


            ContentValues cv = new ContentValues();
            cv.put(DBHelper.DBHelperItem.COLUMN_NAME_TRANSFER_STATUS,TransferUtils.STATUS_DATA_TRANSFER_COMPLETE);
            dataBase.updateRecord(cv,item.getId());
        }


        WearManager wearManager = WearManager.getInstance();
        Set<Node> nodes = wearManager.getConnectedNodes();
        if (nodes == null) {
            return;
        }
        Set<Node> nearbyNodes = new NearbyFilter().filterNodes(nodes);
        DataMap dataMap = new DataMap();
        String data=dataBatch.toString();
        //dataMap.putInt(Constants.KEY_PAGE, page);
        dataMap.putByteArray(Constants.KEY_DATA,data.getBytes(DEFAULT_CHARSET));
        for(Node node : nearbyNodes) {
            wearManager.sendMessage(node.getId(), Constants.DATA_PATH_WEAR, dataMap, null);
        }
        ///TransferAudioClip(items.get(0).getName());

        /*WearFileTransfer fileTransferLowLevel = new WearFileTransfer.Builder(
                targetNode)
                .setOnChannelOutputStreamListener(
                        new OutputStreamListener(items.get(0).getName(),
                                AudioFileSyncActivity.this))
                .build();
        fileTransferLowLevel.requestOutputStream();*/

    }



    /**
     * A listener that is called when we have a channel open and an {@code OutputStream} ready
     */
    private class OutputStreamListener
            implements WearFileTransfer.OnWearableChannelOutputStreamListener {

        private final WearFileTransfer.OnChannelTransferProgressListener mProgressListener;
        //private final int mResourceId;
        private final String mResourceName;

        OutputStreamListener(String resourceId,
                             WearFileTransfer.OnChannelTransferProgressListener progressListener) {
            //mResourceId = resourceId;
            mResourceName = resourceId;
            mProgressListener = progressListener;
        }

        @Override
        public void onOutputStreamForChannelReady(final int statusCode, final Channel channel,
                                                  final OutputStream outputStream) {

            if (statusCode != WearableStatusCodes.SUCCESS) {
                Log.e(TAG, "Failed to open a channel, status code: " + statusCode);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    final byte[] buffer = new byte[BUFFER_SIZE];
                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                    int transferred = 0;
                    int nRead;
                    try {
                        //InputStream is = getResources().openRawResource(mResourceId);
                        InputStream is = openFileInput(mResourceName);
                        long fileSize = is.available();
                        Log.d(TAG,"file size="+fileSize);
                        bis = new BufferedInputStream(is);
                        while ((nRead = bis.read(buffer)) != -1) {
                            bos.write(buffer);
                            transferred += nRead;
                            if (mProgressListener != null) {
                                mProgressListener.onProgressUpdated(transferred, fileSize);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "startTransfer(): IO Error while reading/writing", e);
                        if (mProgressListener != null) {
                            mProgressListener.onProgressUpdated(0, 0);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AudioFileSyncActivity.this,
                                        R.string.failed_to_transfer_bytes, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    } finally {
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }

                        try {
                            bos.close();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * Called on a non-UI thread
     */
    @Override
    public void onProgressUpdated(final long progress, final long max) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setMax((int) max);
                mProgressBar.setProgress((int) progress);
            }
        });
    }

    /**
     * Copies a file from the assets directory to the internal application's file storage so we can
     * get a hold of it as a {@link File} object. It returns a {@link File} reference to the
     * file.
     */
    private File copyFileToPrivateDataIfNeededAndReturn (String fileName) {
        File file = new File(this.getFilesDir(), fileName);
        if (file.exists()) {
            Log.d(TAG, "File already exists in the target location");
            return file;
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = getAssets().open(fileName);
            fileOutputStream = new FileOutputStream(file);

            byte buffer[] = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            Log.d(TAG, "File was successfully moved to " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Failed to access files", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register our listener
        mWearManager.addWearConsumer(mWearConsumer);
        WearApplication.setPage(Constants.TARGET_FILE_TRANSFER);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove our listener
        mWearManager.removeWearConsumer(mWearConsumer);
    }

}
