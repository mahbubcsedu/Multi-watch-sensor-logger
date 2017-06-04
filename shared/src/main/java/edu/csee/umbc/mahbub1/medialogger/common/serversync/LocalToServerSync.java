package edu.csee.umbc.mahbub1.medialogger.common.serversync;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.csee.umbc.mahbub1.medialogger.common.data.WearableSensorDataList;
import edu.csee.umbc.mahbub1.medialogger.common.database.WearableSensorData;
import edu.csee.umbc.mahbub1.medialogger.common.utils.DataTransferUtils;
import edu.csee.umbc.mahbub1.medialogger.common.utils.TransferSingleTon;
import edu.csee.umbc.mahbub1.medialogger.common.data.Data;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * Created by mahbub on 3/28/17.
 */

public class LocalToServerSync {
    public static final String TAG =LocalToServerSync.class.getSimpleName();
    private final int NETWORK_TRANSFER_TIMEOUT_MS = 60 * 1000;
    private int dataCounter;
    //private ProgressDialog progressDialog;

    private Context mContext;
    public LocalToServerSync(Context context){
        this.mContext = context;
        this.dataCounter=0;

    }
    public int getDataCounter(){
        return this.dataCounter;
    }
    public void syncDataToServerAsJson(List<Data> dataList, String androidDevice, long id)
    {
        final String URL = DataTransferUtils.SERVER_URL;
        List<WearableSensorData> listData=new ArrayList<>();
        this.dataCounter += dataList.size();

        for(Data dataItem: dataList){
            WearableSensorData wearableSensorData = new WearableSensorData();
            wearableSensorData.setDatasource(dataItem.getSource());
            wearableSensorData.setAccuracy(dataItem.getAccuracy());
            wearableSensorData.setTimestamp(dataItem.getTimestamp());
            wearableSensorData.setAndroidDevice(androidDevice);
            wearableSensorData.setTbId(id);


            if (dataItem.getValues().length > 0) {
                wearableSensorData.setVal1(dataItem.getValues()[0]);
            } else {
                wearableSensorData.setVal1(0.0f);

            }

            if (dataItem.getValues().length > 1) {

                wearableSensorData.setVal2(dataItem.getValues()[1]);
            } else {
                wearableSensorData.setVal2(0.0f);

            }

            if (dataItem.getValues().length > 2) {

                wearableSensorData.setVal3(dataItem.getValues()[2]);
            } else {
                wearableSensorData.setVal3(0.0f);

            }

            if (dataItem.getValues().length > 3) {

                wearableSensorData.setVal4(dataItem.getValues()[3]);
            } else {
                wearableSensorData.setVal4(0.0f);

            }

            if (dataItem.getValues().length > 4) {

                wearableSensorData.setVal5(dataItem.getValues()[4]);
            } else {
                wearableSensorData.setVal5(0.0f);

            }
            if (dataItem.getValues().length > 5) {

                wearableSensorData.setVal6(dataItem.getValues()[5]);
            } else {
                wearableSensorData.setVal6(0.0f);

            }
            if (dataItem.getValues().length > 6) {

                wearableSensorData.setVal7(dataItem.getValues()[6]);
            } else {
                wearableSensorData.setVal7(0.0f);

            }
            if (dataItem.getValues().length > 7) {

                wearableSensorData.setVal8(dataItem.getValues()[7]);
            } else {
                wearableSensorData.setVal8(0.0f);

            }
            if (dataItem.getValues().length > 8) {

                wearableSensorData.setVal9(dataItem.getValues()[8]);
            } else {
                wearableSensorData.setVal9(0.0f);

            }

            listData.add(wearableSensorData);
        }

        //ArrayList<WearableSensorData> result = retrieveDatatoTransfer();
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(listData, new TypeToken<List<WearableSensorData>>() {
        }.getType());

        if (!element.isJsonArray()) {

        }

        JsonArray jsonArray = element.getAsJsonArray();
        final String jsonData = jsonArray.toString();



        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                syncUpdateRealm(response);
                Log.i("VOLLEY", response);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
                //progressDialog.dismiss();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return jsonData == null ? null : jsonData.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", jsonData, "utf-8");
                    return null;
                }
            }

            /* @Override
             protected Response<String> parseNetworkResponse(NetworkResponse response) {
                 String responseString = "";
                 if (response != null) {
                     responseString = String.valueOf(response.statusCode);

                     // can get more details such as response.headers
                 }
                 return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
             }*/
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };


        stringRequest.setRetryPolicy(new DefaultRetryPolicy(NETWORK_TRANSFER_TIMEOUT_MS, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // add the request object to the queue to be executed
        TransferSingleTon.getInstance(this.mContext).addToRequestQueue(stringRequest);

    }

    public RealmResults<WearableSensorDataList> retrieveDatatoTransfer() {


        Realm realm = Realm.getDefaultInstance();
        RealmQuery<WearableSensorDataList> query = realm.where(WearableSensorDataList.class);
        query.equalTo("status", DataTransferUtils.STATUS_DATA_TRANSFER_INCOMPLETE);
        query.or().equalTo("status", DataTransferUtils.STATUS_DATA_TRANSFER_IN_QUEUE);
        query.or().equalTo("status", DataTransferUtils.STATUS_DATA_TRANSFER_READY);

        final RealmResults<WearableSensorDataList> unsyncData = query.findAll();
       /*realm.executeTransaction(new Realm.Transaction() {
           @Override
           public void execute(Realm realm) {

                unsyncData = realm.where(WearableSensorDataList.class)
                       .equalTo("status", DataTransferUtils.STATUS_DATA_TRANSFER_INCOMPLETE).findAll();
           }});*/

        return unsyncData;


    }

     void syncUpdateRealm(String jsonResponse){
        //long table_row_id=-1;
        try {
            JSONObject jsonObj = new JSONObject(jsonResponse);

            try {
                if (jsonObj.getString("error").equals("")) {
                    Log.d(TAG,"successfully transfered");
                    final long table_row_id = Long.parseLong(jsonObj.getString("rows"));

                    Realm realm = Realm.getDefaultInstance();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            WearableSensorDataList wearableSensorDataList = new WearableSensorDataList();
                            wearableSensorDataList.setId(table_row_id);
                            wearableSensorDataList.setStatus(DataTransferUtils.STATUS_DATA_TRANSFER_COMPLETE);

                            realm.copyToRealmOrUpdate(wearableSensorDataList);


                        }
                    });

                } else {
                    String error_message = jsonObj.getString("error");
                    Log.d(TAG, "error occurred:"+error_message);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } catch (JSONException e) {
            //System.out.println("its not a json object and can't parse" + e.getMessage());
            //e.printStackTrace();
        }



    }

}
