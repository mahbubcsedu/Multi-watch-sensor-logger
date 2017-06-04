package edu.csee.umbc.mahbub1.medialogger.common.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by mahbub on 3/31/17.
 */

public class Status {
long lastUpdateTime = -1;

    public void updated(){
        lastUpdateTime = System.currentTimeMillis();
    }

    public boolean hasBeenUpdatedSince(long timeStamp){
        return lastUpdateTime > timeStamp;
    }

    @Override
    public String toString(){
        return toJson();
    }

    public String toJson(){
        String dataInJsonFormat = null;

        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            dataInJsonFormat = mapper.writeValueAsString(this);
        }catch(Exception e){
            e.printStackTrace();
        }
        return  dataInJsonFormat;
    }

    public long getLastUpdateTime(){
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime){
        this.lastUpdateTime = lastUpdateTime;
    }
}
