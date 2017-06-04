package edu.csee.umbc.mahbub1.medialogger.common.loggin;

/**
 * Created by mahbub on 4/4/17.
 */

import java.util.ArrayList;
import java.util.List;

public final class TrackerManager {

    public static final String KEY_CONNECTION_SPEED_TEST = "Connection Speed Test";

    List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> timeTrackers;

    public TrackerManager() {
        timeTrackers = new ArrayList<>();
    }

    public List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> getActiveTrackers() {
        return getActiveTrackers(timeTrackers);
    }

    /**
     * Returns a list of trackers that have been started but not stopped yet
     */
    public static List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> getActiveTrackers(List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> timeTrackers) {
        List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> activeTrackers = new ArrayList<>();
        for (edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker timeTracker : timeTrackers) {
            if (timeTracker.isTracking()) {
                activeTrackers.add(timeTracker);
            }
        }
        return activeTrackers;
    }

    public edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker getTracker(String key) {
        return getTracker(key, true);
    }

    public edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker getTracker(String key, boolean createIfNotExisting) {
        edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker tracker = getTracker(key, timeTrackers);
        if (tracker == null && createIfNotExisting) {
            tracker = new edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker(key);
            timeTrackers.add(tracker);
            return tracker;
        } else {
            return tracker;
        }
    }

    public static edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker getTracker(String key, List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> timeTrackers) {
        for (edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker timeTracker : timeTrackers) {
            if (timeTracker.getKey().equals(key)) {
                return timeTracker;
            }
        }
        return null;
    }

    public void removeTracker(String key) {
        edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker tracker = getTracker(key, timeTrackers);
        if (tracker != null) {
            timeTrackers.remove(tracker);
        }
    }

    /**
     * Getter & Setter
     */
    public List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> getTimeTrackers() {
        return timeTrackers;
    }

    public void setTimeTrackers(List<edu.csee.umbc.mahbub1.medialogger.common.loggin.TimeTracker> timeTrackers) {
        this.timeTrackers = timeTrackers;
    }
}

