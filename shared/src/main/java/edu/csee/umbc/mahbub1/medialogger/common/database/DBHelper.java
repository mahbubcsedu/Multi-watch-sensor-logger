package edu.csee.umbc.mahbub1.medialogger.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Comparator;

import edu.csee.umbc.mahbub1.medialogger.common.TransferUtils;

//import edu.csee.umbc.mahbub1.medialogger.common.TransferUtils;


/**
 * Created by Daniel on 12/29/2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String LOG_TAG = "DBHelper";

    private static edu.csee.umbc.mahbub1.medialogger.common.database.OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME = "saved_recordings";

        public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
        public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
        public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
        public static final String COLUMN_NAME_TRANSFER_STATUS="status";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBHelperItem.TABLE_NAME + " (" +
                    DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_TRANSFER_STATUS + TEXT_TYPE + COMMA_SEP +
                    DBHelperItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";

    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(edu.csee.umbc.mahbub1.medialogger.common.database.OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public RecordingItemDb getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                DBHelperItem.COLUMN_NAME_TRANSFER_STATUS,
                DBHelperItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            RecordingItemDb item = new RecordingItemDb();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setName(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
            item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
            item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
            item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
            item.setmTransStatus(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TRANSFER_STATUS)));
            c.close();
            return item;
        }
        return null;
    }
    public ArrayList<RecordingItemDb> getItems(String selections) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<RecordingItemDb> items =new ArrayList<>();
        String[] projection = {
                DBHelperItem._ID,
                DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                DBHelperItem.COLUMN_NAME_TIME_ADDED,
                DBHelperItem.COLUMN_NAME_TRANSFER_STATUS,
                DBHelperItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, selections, null, null, null, null);
        RecordingItemDb item;
        try {
            while (c.moveToNext()) {
                item = new RecordingItemDb();
                item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
                item.setName(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
                item.setFilePath(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
                item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
                item.setLength(c.getInt(c.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
                item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
                //c.close();
                items.add(item);
            }
        } finally {
            c.close();
        }
        return items;
    }
    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };
        db.delete(DBHelperItem.TABLE_NAME, "_ID=?", whereArgs);
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<RecordingItemDb> {
        public int compare(RecordingItemDb item1, RecordingItemDb item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long addRecording(String recordingName, String filePath, long length) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, length);
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        cv.put(DBHelperItem.COLUMN_NAME_TRANSFER_STATUS, TransferUtils.STATUS_DATA_TRANSFER_INCOMPLETE);
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }
    public long addRecording(ContentValues contentValues){
        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, contentValues);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;

    }
    public void renameItem(RecordingItemDb item, String recordingName, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
        db.update(DBHelperItem.TABLE_NAME, cv,
                DBHelperItem._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public int updateRecord(ContentValues cv, int rowId) {
        SQLiteDatabase db = getWritableDatabase();
        //ContentValues cv = new ContentValues();
        if(cv.size()<1)
            return -1;
        else {
            //cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
            //cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
            int updateID=db.update(DBHelperItem.TABLE_NAME, cv,
                    DBHelperItem._ID + "=" + rowId, null);

            if (mOnDatabaseChangedListener != null) {
                mOnDatabaseChangedListener.onDatabaseEntryRenamed();
            }
            return updateID;
        }
    }

    public long restoreRecording(RecordingItemDb item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, item.getName());
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, item.getFilePath());
        cv.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, item.getLength());
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(DBHelperItem.COLUMN_NAME_TRANSFER_STATUS, item.getmTransStatus());
        cv.put(DBHelperItem._ID, item.getId());
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }
}
