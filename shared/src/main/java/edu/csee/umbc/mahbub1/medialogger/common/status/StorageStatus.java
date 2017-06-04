package edu.csee.umbc.mahbub1.medialogger.common.status;

/**
 * Created by mahbub on 4/18/17.
 */

public class StorageStatus extends Status{
    private boolean storageOnLocalWatch = false;
    private boolean finishedUnsentData=false;

    public StorageStatus() {
        this.storageOnLocalWatch = false;
    }

    public boolean isStorageOnLocalWatch() {
        return storageOnLocalWatch;
    }

    public void setStorageOnLocalWatch(boolean storageOnLocalWatch) {
        this.storageOnLocalWatch = storageOnLocalWatch;
    }

    public boolean isFinishedUnsentData() {
        return finishedUnsentData;
    }

    public void setFinishedUnsentData(boolean finishedUnsentData) {
        this.finishedUnsentData = finishedUnsentData;
    }
}
