package edu.csee.umbc.mahbub1.medialogger.common.status;

/**
 * Created by mahbub on 4/17/17.
 */

public class ActivityStatus extends Status {

    private boolean activityInitialized = false;
    private boolean activityInForGround = true;
    private boolean deviceInambientMode=false;

    public boolean isActivityInitialized() {
        return activityInitialized;
    }

    public void setActivityInitialized(boolean initialized) {
        this.activityInitialized = initialized;
    }

    public boolean isActivityInForGround() {
        return activityInForGround;
    }

    public void setActivityInForGround(boolean inForeground) {
        this.activityInForGround = inForeground;
    }

    public boolean isDeviceInambientMode() {
        return deviceInambientMode;
    }

    public void setDeviceInambientMode(boolean ambientMode) {
        this.deviceInambientMode = ambientMode;
    }
}
