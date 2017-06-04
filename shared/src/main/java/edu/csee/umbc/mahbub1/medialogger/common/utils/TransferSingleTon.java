package edu.csee.umbc.mahbub1.medialogger.common.utils;

/**
 * Created by mahbub on 3/28/17.
 */

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by mahbub on 2/15/17.
 */

public class TransferSingleTon {
    public static final String TAG=TransferSingleTon.class.getSimpleName();
    private static TransferSingleTon mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private TransferSingleTon(Context context) {
        mCtx = context;
        mRequestQueue = getmRequestQueue();


    }

    public static synchronized TransferSingleTon getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TransferSingleTon(context);
        }
        return mInstance;
    }

    public RequestQueue getmRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag){
        request.setTag(TextUtils.isEmpty(tag)? TAG : tag);
        VolleyLog.d("Adding request to queue %s", request.getUrl());
        getmRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request){
        request.setTag(TAG);
        getmRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag){
        if(mRequestQueue!=null){
            mRequestQueue.cancelAll(tag);
        }
    }
}
