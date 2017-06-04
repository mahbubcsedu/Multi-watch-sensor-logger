package edu.csee.umbc.mahbub1.medialogger.common.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mahbub on 4/4/17.
 */

public class SourceSensorTypeMap {
    public static final String TAG = SourceSensorTypeMap.class.getSimpleName();
    //how many sensor a device could have;
    private static final int MAX_SENSOR_SUPPORT=25;
    private Map<String,Integer> mSensorTypeSourceMap;
    public ArrayList<String> mSourceNodes;

    public SourceSensorTypeMap(){
        this.mSensorTypeSourceMap = new HashMap<>();
        mSourceNodes = new ArrayList<String>();

    }

    public int addNewSensor(String compositeID){
        if(!mSensorTypeSourceMap.containsKey(compositeID))
            //max sensor +
            mSensorTypeSourceMap.put(compositeID,mSensorTypeSourceMap.size());

        return mSensorTypeSourceMap.get(compositeID);

    }

    public void addNewSourceNode(String newSourceNode)
    {
        if(!mSourceNodes.contains(newSourceNode))
            mSourceNodes.add(newSourceNode);

        //return mSourceNodes.size();
    }

    public int getSourceNodePosition(String newSourceNode){
        if(!mSourceNodes.contains(newSourceNode))
        {
            addNewSourceNode(newSourceNode);
        }
        return mSourceNodes.indexOf(newSourceNode);

    }
}