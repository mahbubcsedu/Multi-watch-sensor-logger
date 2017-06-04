package edu.csee.umbc.mahbub1.medialogger.common.database;

/**
 * Created by mahbub on 2/6/17.
 */

public class WearableSensorData {
    private String android_device;
    private long sensorTimestamp;
    private float val1;
    private float val2;
    private float val3;
    private float val4;
    private float val5;
    private float val6;
    private float val7;
    private float val8;
    private float val9;
    private long timestamp;
    //private float z;
    private int accuracy;

    private String datasource; //sensor_name
    //private long sensorType;
    private long tbId;

    public long getSensorTimestamp() {
        return sensorTimestamp;
    }

    public void setSensorTimestamp(long sensorTimestamp) {
        this.sensorTimestamp = sensorTimestamp;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public String getAndroidDevice() {
        return android_device;
    }

    public void setAndroidDevice(String androidDevice) {
        this.android_device = androidDevice;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTbId() {
        return tbId;
    }

    public void setTbId(long tbId) {
        this.tbId = tbId;
    }

    public float getVal1() {
        return val1;
    }

    public void setVal1(float val1) {
        this.val1 = val1;
    }

    public float getVal2() {
        return val2;
    }

    public void setVal2(float val2) {
        this.val2 = val2;
    }

    public float getVal3() {
        return val3;
    }

    public void setVal3(float val3) {
        this.val3 = val3;
    }

    public float getVal4() {
        return val4;
    }

    public void setVal4(float val4) {
        this.val4 = val4;
    }

    public float getVal5() {
        return val5;
    }

    public void setVal5(float val5) {
        this.val5 = val5;
    }

    public float getVal6() {
        return val6;
    }

    public void setVal6(float val6) {
        this.val6 = val6;
    }

    public float getVal7() {
        return val7;
    }

    public void setVal7(float val7) {
        this.val7 = val7;
    }

    public float getVal8() {
        return val8;
    }

    public void setVal8(float val8) {
        this.val8 = val8;
    }

    public float getVal9() {
        return val9;
    }

    public void setVal9(float val9) {
        this.val9 = val9;
    }


}
