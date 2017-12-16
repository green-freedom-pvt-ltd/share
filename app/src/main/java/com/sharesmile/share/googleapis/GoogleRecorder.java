package com.sharesmile.share.googleapis;

import com.sharesmile.share.gps.models.WorkoutData;

/**
 * Created by ankitmaheshwari on 12/15/17.
 */

public interface GoogleRecorder {

    void start();
    /**
     * Synchronously update the result object with recorded data and stops the recording
     * Should not be called on main thread
     * @param result
     */
    void readAndStop(WorkoutData result);
    void pause();
    void resume();

}
