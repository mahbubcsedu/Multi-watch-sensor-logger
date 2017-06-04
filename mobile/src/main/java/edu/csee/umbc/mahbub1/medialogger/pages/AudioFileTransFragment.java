package edu.csee.umbc.mahbub1.medialogger.pages;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.csee.umbc.mahbub1.medialogger.MobileApplication;
import edu.csee.umbc.mahbub1.medialogger.R;
import edu.csee.umbc.mahbub1.medialogger.common.Constants;
import edu.csee.umbc.mahbub1.medialogger.common.TransferUtils;
import edu.csee.umbc.mahbub1.medialogger.common.data.AudioData;
import edu.csee.umbc.mahbub1.medialogger.common.data.AudioDataBatch;
import edu.csee.umbc.mahbub1.medialogger.common.database.DBHelper;



/**
 * Created by mahbub on 4/28/17.
 */

public class AudioFileTransFragment extends Fragment {
    private static final String TAG = "AudioFileTransFragment";
    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private ImageView mImageView;
    private TextView mTextView;
    private AsyncTask<Void, Void, Bitmap> mAsyncTask;
    private ProgressBar mProgressBar;
    private DBHelper database;
    private Map<String,AudioData> receivedData;
    private int numberOfFileTransferred, numberofData;
    boolean isAudioInfoReceived = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpWearListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_transfer, container, false);
        mImageView = (ImageView) view.findViewById(R.id.image);
        mTextView = (TextView) view.findViewById(R.id.text);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        view.findViewById(R.id.clear_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setImageResource(R.drawable.ic_photo_200dp);
            }
        });
        view.findViewById(R.id.clear_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText(R.string.text_file_here);
            }
        });

        database = new DBHelper(getActivity().getApplicationContext());
        receivedData = new HashMap<>();
        this.numberOfFileTransferred=0;
        this.numberofData=0;
        isAudioInfoReceived=false;
        return view;
    }

    /**
     * Creates two listeners to be called when the transfer of the text file is completed and when
     * a channel and an input stream is available to receive the image file. In some cases, it is
     * desired to be transfer files even if the application at the receiving node is not in front.
     * In those cases, one can define the same {@link com.google.devrel.wcl.callbacks.WearConsumer}
     * in the application instance; then the WearableListener that the WCL library provides will be
     * able to handle the transfer.
     */
    private void setUpWearListeners() {
        mWearManager = WearManager.getInstance();
        mWearConsumer = new AbstractWearConsumer() {

           /* @Override
            public void onWearableInputStreamForChannelOpened(int statusCode, String requestId,
                                                              final Channel channel, final InputStream inputStream) {
                if (statusCode != WearableStatusCodes.SUCCESS) {
                    Log.e(TAG, "onWearableInputStreamForChannelOpened(): "
                            + "Failed to get input stream");
                    return;
                }
                Log.d(TAG, "Channel opened for path: " + channel.getPath());
                mAsyncTask = new AsyncTask<Void, Void, Bitmap>() {

                    @Override
                    protected void onPreExecute() {
                        mImageView.setImageResource(R.drawable.ic_photo_200dp);
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        closeStreams();
                        if (isCancelled()) {
                            return null;
                        }
                        return bitmap;
                    }

                    @Override
                    protected void onCancelled() {
                        mProgressBar.setVisibility(View.GONE);
                        mAsyncTask = null;
                        closeStreams();
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        mProgressBar.setVisibility(View.GONE);
                        mImageView.setImageBitmap(bitmap);
                        mAsyncTask = null;
                    }

                    public void closeStreams() {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            // no-op
                        }
                    }
                };
                mAsyncTask.execute();
            }*/

            @Override
            public void onWearableFileReceivedResult(int statusCode, String requestId,
                                                     File savedFile, String originalName) {
                Log.d(TAG, String.format(
                        "File Received: status=%d, requestId=%s, savedLocation=%s, originalName=%s",
                        statusCode, requestId, savedFile.getAbsolutePath(), originalName));
                try {

                   // String destinationPath= TransferUtils.setFileNameAndPath(getActivity());
                    //database.addRecording(originalName,destinationPath,0);

                    receivedData.put(originalName, null);

                    Log.d(TAG,"record added to database");
                    try {
                        //TransferUtils.getAlbumStorageDir("cliprecorder");
                        copyFile(savedFile.getAbsoluteFile(), new File(TransferUtils.getAlbumStorageDir("cliprecorder") + File.separator + originalName));
                        //copyFile(savedFile.getAbsoluteFile(), new File(TransferUtils.getAlbumStorageDir("Recording") +File.separator+ originalName));

                    numberOfFileTransferred++;

                }catch (IOException e){
                    Log.d(TAG, "failed to move the file");
                }
                }catch (Exception e){
                    Log.d(TAG,"copy failed" + e.getLocalizedMessage());
                }


                String fileContent = getSimpleTextFileContent(savedFile);
                mTextView.setText("something came");
            }

            public void updateDatabase( ){
                //if((numberOfFileTransferred==numberofData) && (numberofData!=0) && (numberOfFileTransferred!=0)){

                ContentValues contentValues= new ContentValues();
                for(Map.Entry<String,AudioData> entry: receivedData.entrySet()){
                    AudioData audioData = (AudioData) entry.getValue();
                    contentValues = new ContentValues();
                    contentValues.put(DBHelper.DBHelperItem.COLUMN_NAME_RECORDING_NAME,audioData.getClipName());
                    contentValues.put(DBHelper.DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, audioData.getLength());
                    contentValues.put(DBHelper.DBHelperItem.COLUMN_NAME_TIME_ADDED,audioData.getTimestamp());
                    database.addRecording(contentValues);
                    Log.d(TAG,"record added using content value"+contentValues.get(DBHelper.DBHelperItem.COLUMN_NAME_RECORDING_LENGTH));

                }

            }
            @Override
            public void onWearableMessageReceived(MessageEvent messageEvent) {
                if (!Constants.DATA_PATH_WEAR.equals(messageEvent.getPath())) {
                    return;
                }

                DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
                //int currentPage  = dataMap.getInt(Constants.KEY_PAGE, Constants.TARGET_INTRO);
                byte[] data = dataMap.getByteArray(Constants.KEY_DATA);
                String dataString=null;
                if (data != null) {
                    dataString= new String(data, TransferUtils.DEFAULT_CHARSET);
                }


                AudioDataBatch audioDataBatch = AudioDataBatch.fromJson(dataString);
                //AudioData audioData = receivedData.get(audioDataBatch);

                numberofData = audioDataBatch.getAudioDataMap().size();

                isAudioInfoReceived=true;
                addOrUpdateRecord(null,audioDataBatch.getAudioDataMap());
                updateDatabase();
                //writeMessage(dataString);
                Log.d(TAG, "message received"+dataString);
                //mTextView.setText(mTextView.getText()+"data received");
                //Toast.makeText(getActivity(),dataString, Toast.LENGTH_LONG).show();
            }

            private void createOrUpdateDataRecord(){

            }
            public  void copyFile(File src, File dst) throws IOException
            {
                Log.d(TAG, "source="+src +" dset="+dst);
                FileChannel inChannel = new FileInputStream(src).getChannel();
                FileChannel outChannel = new FileOutputStream(dst).getChannel();
                try
                {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
                finally
                {
                    if (inChannel != null)
                        inChannel.close();
                    if (outChannel != null)
                        outChannel.close();
                }
            }

            private void addOrUpdateRecord(String clipName, Map<String,AudioData> audioDataMap){
                if(audioDataMap==null) { //if if null data provided with only key but key is not present just add the key with empty audio object

                    if(!receivedData.containsKey(clipName))
                    {
                    AudioData audioData = new AudioData();
                    audioData.setTransferredToMobile(true);

                    receivedData.put(clipName,audioData);
                    }else{
                        receivedData.get(clipName).setTransferredToMobile(true);
                    }

                } else if(audioDataMap!=null && !receivedData.containsKey(clipName)){
                    receivedData.putAll(audioDataMap);
                }
            }

        };
    }

    /**
     * A rudimentary method to read the content of the {@code file}.
     */
    private String getSimpleTextFileContent(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("file is null or doesn't exists!");
        }
        try {
            return new Scanner(file).useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            // already captured
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register our listeners
        mWearManager.addWearConsumer(mWearConsumer);

        // add the local capability to handle file transfer
        mWearManager.addCapabilities(Constants.CAPABILITY_FILE_PROCESSOR);

        MobileApplication.setPage(Constants.TARGET_FILE_TRANSFER);
    }

    @Override
    public void onPause() {
        // unregister our listeners
        mWearManager.removeWearConsumer(mWearConsumer);

        // remove the capability to handle file transfer
        mWearManager.removeCapabilities(Constants.CAPABILITY_FILE_PROCESSOR);

        /*if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }*/
        super.onPause();
    }
}
