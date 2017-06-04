package edu.csee.umbc.mahbub1.medialogger.common.utils;

/**
 * Created by mahbub on 2/27/17.
 */

public class MessagePath {
    public static final String TAG = MessagePath.class.getSimpleName();

    public static final String KEY_PATH = "path";
    public static final String KEY_DATA = "data";
    public static final String KEY_SOURCE_NODE_ID = "source_node_id";

    public static final String PATH_ANY = "/*";
    public static final String PATH_PING = "/ping";
    public static final String PATH_ECHO = "/echo";
    public static final String PATH_CLOSING = "/closing";
    public static final String PATH_GET_STATUS = "/get_status";
    public static final String PATH_SET_STATUS = "/set_status";
    public static final String PATH_GET_SENSORS = "/get_sensors";
    public static final String PATH_SET_SENSORS = "/set_sensors";
    public static final String PATH_SENSOR_DATA_REQUEST = "/sensor_data_request";
    public static final String PATH_SENSOR_DATA_REQUEST_RESPONSE = "/sensor_data_request_response";
    public static final String START_MEASUREMENT = "/start";
    public static final String STOP_MEASUREMENT = "/stop";
    public static final String PATH_FILTER="/filter";
    public static final String PATH_WEARABLE_DATA="/datapath/";

}
