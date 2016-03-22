package com.lonelyship.line_toy;

import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by LONELYSHIP on 2015/4/27.
 */
public class SoundMeter {

    private MediaRecorder mRecorder = null;

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }
}
