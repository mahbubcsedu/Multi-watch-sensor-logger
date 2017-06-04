package edu.csee.umbc.mahbub1.medialogger.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mahbub on 4/29/17.
 */

public class AudioDataBatch {
    //List<AudioData> audioDataList;
    Map<String, edu.csee.umbc.mahbub1.medialogger.common.data.AudioData> audioDataMap;

    public AudioDataBatch(Map<String, edu.csee.umbc.mahbub1.medialogger.common.data.AudioData> audioDataMap) {
        this.audioDataMap = audioDataMap;
    }

    public AudioDataBatch() {
        this.audioDataMap = new HashMap<>();
    }

    public void addData(edu.csee.umbc.mahbub1.medialogger.common.data.AudioData data) {
        audioDataMap.put(data.getClipName(),data);
        //trimDataToCapacity();
    }

    public void addData(Map<String, edu.csee.umbc.mahbub1.medialogger.common.data.AudioData> data) {
        audioDataMap.putAll(data);
        //trimDataToCapacity();
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
    public static AudioDataBatch fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            AudioDataBatch dataRequestResponse = mapper.readValue(json, AudioDataBatch.class);
            return dataRequestResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    /**
     * Getter & Setter
     */
    public Map<String, edu.csee.umbc.mahbub1.medialogger.common.data.AudioData> getAudioDataMap() {
        return audioDataMap;
    }


}
