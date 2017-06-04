package edu.csee.umbc.mahbub1.medialogger.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

public class DataRequestResponse {

    private List<edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch> dataBatches;
    private long startTimestamp;
    private long endTimestamp;
    private long dataSize;

    public DataRequestResponse() {
    }

    public DataRequestResponse(List<edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch> dataBatches) {
        this.dataBatches = dataBatches;
        endTimestamp = System.currentTimeMillis();
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

    public static DataRequestResponse fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            DataRequestResponse dataRequestResponse = mapper.readValue(json, DataRequestResponse.class);
            return dataRequestResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch> getDataBatches() {
        return dataBatches;
    }


    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public void setDataBatches(List<edu.csee.umbc.mahbub1.medialogger.common.data.DataBatch> dataBatches) {
        this.dataBatches = dataBatches;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
}
