package edu.csee.umbc.mahbub1.medialogger.common.sensors;

import android.hardware.SensorManager;
import android.util.SparseArray;

/**
 * Created by mahbub on 4/20/17.
 */

public class SensorRecordFrequency {

    public SparseArray<Integer> frequency_mode;

    public SensorRecordFrequency(){
        frequency_mode = new SparseArray<>();

        frequency_mode.append(0, SensorManager.SENSOR_DELAY_FASTEST);
        frequency_mode.append(1,SensorManager.SENSOR_DELAY_GAME);
        frequency_mode.append(2,SensorManager.SENSOR_DELAY_UI);
        frequency_mode.append(3,SensorManager.SENSOR_DELAY_NORMAL);
        frequency_mode.append(4,4);

    }

    public int getName(int sensorId) {
        Integer fmode = frequency_mode.get(sensorId);

        if (fmode == null) {
            fmode = -1;
        }

        return fmode;
    }

    public SparseArray<Integer> getNames() {
        return frequency_mode;
    }
}
