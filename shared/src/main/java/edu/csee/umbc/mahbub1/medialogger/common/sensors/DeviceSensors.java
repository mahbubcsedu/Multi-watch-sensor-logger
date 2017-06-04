package edu.csee.umbc.mahbub1.medialogger.common.sensors;

import android.hardware.Sensor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahbub on 2/27/17.
 */

public class DeviceSensors implements Serializable{
    List<Wears> sensors;

    public DeviceSensors() {
        sensors = new ArrayList<>();
    }

    public DeviceSensors(List<Wears> sensors) {
        this();
        this.sensors.addAll(sensors);
    }

    public DeviceSensors(List<Sensor> hardwareSensors, boolean includeWakeUpSensors) {
        this();
        for (Sensor sensor : hardwareSensors) {
            Wears deviceSensor = new Wears(sensor);
            if (!includeWakeUpSensors && deviceSensor.isWakeUpSensor()) {
                continue;
            }
            sensors.add(deviceSensor);
        }
    }

    @JsonIgnore
    public List<Wears> getNonWakeupSensors() {
        return filterWakeUpSensors(sensors);
    }

    public static List<Wears> filterWakeUpSensors(List<Wears> sensors) {
        List<Wears> nonWakeUpSensors = new ArrayList<>();
        for (Wears sensor : sensors) {
            if (sensor.isWakeUpSensor()) {
                continue;
            }
            nonWakeUpSensors.add(sensor);
        }
        return nonWakeUpSensors;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return toJson();
    }

    @JsonIgnore
    public String toJson() {
        String jsonData = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jsonData = mapper.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }

    public static DeviceSensors fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            DeviceSensors deviceSensors = mapper.readValue(json, DeviceSensors.class);
            return deviceSensors;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Getter & Setter
     */
    public List<Wears> getSensors() {
        return sensors;
    }

    public void setSensors(List<Wears> sensors) {
        this.sensors = sensors;
    }
}
