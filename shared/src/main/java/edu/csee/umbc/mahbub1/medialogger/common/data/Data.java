package edu.csee.umbc.mahbub1.medialogger.common.data;

/**
 * Created by mahbub on 2/27/17.
 */

public class Data {
    private long timestamp;
    private String source;
    private float[] values;
    private int accuracy;
    private long sensorTimeStampNonoS;

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public Data() {
        timestamp = System.currentTimeMillis();
    }

    public Data(float[] values) {
        this();
        this.values = values;
    }

    public Data(String source, float[] values) {
        this(values);
        this.source = source;
    }

    /**
     * Getter & Setter
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public float[] getValues() {
        return values;
    }

    public long getSensorTimeStampNonoS() {
        return sensorTimeStampNonoS;
    }

    public void setSensorTimeStampNonoS(long sensorTimeStampNonoS) {
        this.sensorTimeStampNonoS = sensorTimeStampNonoS;
    }

    public void setValues(float[] values) {
        this.values = values;
    }
}
