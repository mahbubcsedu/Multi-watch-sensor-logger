package edu.csee.umbc.mahbub1.medialogger;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mahbub on 5/2/17.
 */

public class DummyActivity extends AppCompatActivity {
    public static final String TAG= DummyActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy_activity);
        Button btn = (Button) findViewById(R.id.testbutton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // TransferUtils.getAlbumStorageDir("soundclipse");

                //if(TransferUtils.isExternalStorageWritable())
                //    Log.d(TAG, "its writable");
                //if(TransferUtils.isExternalStorageReadable())
                //    Log.d(TAG,"readable");
                File f=getAlbumStorageDir("recordable");
                String content = "hello world";
                File file;
                FileOutputStream outputStream;
                try{
                    file = new File(f.getAbsoluteFile(),"test.txt");
                    outputStream = new FileOutputStream(file);
                    outputStream.write(content.getBytes());
                    outputStream.close();

                }catch (IOException e){
                    Log.d(TAG, "file write failed");
                }

            }
        });
    }

    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), albumName);

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

}
