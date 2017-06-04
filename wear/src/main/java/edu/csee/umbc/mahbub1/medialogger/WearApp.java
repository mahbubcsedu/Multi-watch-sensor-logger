package edu.csee.umbc.mahbub1.medialogger;

import android.app.Application;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by mahbub on 3/30/17.
 */

public class WearApp extends Application {

    private static WearApp wearapplicationInstance;
    private boolean storeToWear=true;
    private GoogleApiClient mGoogleApiClient;
    private static final int CLIENT_CONNECTION_TIMEOUT = 15000;// its in miliseconds

    @Override
    public void onCreate(){
        super.onCreate();
        setupRealmDatabase();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(Wearable.API).build();
        wearapplicationInstance = this;


    }

    public static int getClientConnectionTimeout() {
        return CLIENT_CONNECTION_TIMEOUT;
    }

    public void setStoreToWear(boolean storeToWear) {
        this.storeToWear = storeToWear;
    }

    private void setupRealmDatabase(){
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration); // Clean slate
        Realm.setDefaultConfiguration(realmConfiguration);

    }

    public static synchronized WearApp getInstance(){
        return wearapplicationInstance;
    }

    public void setWearStorage(boolean storeToWear){
        this.storeToWear = storeToWear;

    }

    public boolean isStoreToWear(){
        return this.storeToWear;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public boolean validateConnectionWithHost() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return connectionResult.isSuccess();
    }
    public boolean isGoogleApiConnected(){
        if(mGoogleApiClient.isConnected()){
            return true;
        }
        return false;
    }

}
