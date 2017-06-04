package edu.csee.umbc.mahbub1.medialogger.playback;

/**
 * Created by mahbub on 5/3/17.
 */

public interface PlaybackListener {
    void onProgress(int progress);
    void onCompletion();
}
