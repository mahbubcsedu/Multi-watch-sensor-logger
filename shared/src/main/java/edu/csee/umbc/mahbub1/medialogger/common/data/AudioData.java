package edu.csee.umbc.mahbub1.medialogger.common.data;

/**
 * Created by mahbub on 4/29/17.
 */

public class AudioData {
    private long timestamp;
    private float length;
    private String clipName;
    private long localId;
    private boolean transferredToMobile;
    //private long wearRecordId;

    public AudioData(){
        transferredToMobile=false;
    }
    public AudioData(long rowId,long timestamp,float length, String clipName){
        this.localId = rowId;
        this.timestamp = timestamp;
        this.length = length;
        this.clipName = clipName;
        transferredToMobile = false;
    }



    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public String getClipName() {
        return clipName;
    }

    public void setClipName(String clipName) {
        this.clipName = clipName;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public boolean isTransferredToMobile() {
        return transferredToMobile;
    }

    public void setTransferredToMobile(boolean transferredToMobile) {
        this.transferredToMobile = transferredToMobile;
    }
}
