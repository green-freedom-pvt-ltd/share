package com.sharesmile.share.core;

/**
 * Created by ankitmaheshwari on 11/10/17.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * A TTS (Text-To-Speech) wrapper with extended functionality, designed to be robust and easy to use.
 *
 * @author Lukas Knuth
 * @version 1.0
 */
public final class TTS extends UtteranceProgressListener {

    private TextToSpeech tts;
    private final AudioManager am;
    private int speech_count = 0;
    private final HashMap<String, String> tts_params = new HashMap<>();
    private final AudioManager.OnAudioFocusChangeListener afl = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };

    @Override
    public void onStart(String utteranceId) {
        // Utterance started
    }

    @Override
    public void onDone(String utteranceId) {
        this.speech_count--;
        if (speech_count == 0){
            // No more speeches are queued, give focus back:
            am.abandonAudioFocus(afl);
        }
        shutdown(true);
    }

    @Override
    public void onError(String utteranceId) {

    }

    public interface InitCallback{
        /**
         * Initialisation was successful, work with the TTS.
         */
        void initSuccess(TTS tts);

        /**
         * There was an error while initialising the engine.
         * @param reason error-number, as returned by {@link android.speech.tts.TextToSpeech.OnInitListener#onInit(int)}.
         */
        void initFail(int reason);
    }

    /**
     * Creates a new Text-To-Speech engine.
     * @param callback will be called, once the engine is initialised and ready for usage.
     * @throws java.lang.IllegalStateException you can't initialize this object with a
     *  {@code context} from an Activity that has not yet completed it's {@link android.app.Activity#onCreate(android.os.Bundle)}
     *  method. Maybe do it in {@link android.app.Activity#onStart()} instead?
     */
    public TTS(Context context, final InitCallback callback){
        // TTS Parameters:
        this.tts_params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
        this.tts_params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "PLACEHOLDER"); // We only need this so the utterance-complete listener gets called...
        // Initialise TTS:
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            @SuppressWarnings("deprecation")
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.ENGLISH);
                    tts.setSpeechRate(0.8f);
                    tts.setOnUtteranceProgressListener(TTS.this);
                    callback.initSuccess(TTS.this);
                } else if (status == TextToSpeech.ERROR) {
                    callback.initFail(status);
                }
            }
        });
    }

    /**
     * Shutdown the TTS-Engine.
     * @param stop_immediately whether the TTS-engine should let any previously queued speeches
     *                         finish, or stop them (and the engine) immediatly.
     */
    public void shutdown(boolean stop_immediately){
        if (stop_immediately){
            tts.stop();
        }
        tts.shutdown();
    }

    /**
     * <p>Queue a text for reading out. This will only queue this text and waits, until any earlier
     *  text's are done playing. Also, Music volume will be lowered (if supported by the current
     *  media-player) while the text is spoken.</p>
     * <p>This method returns immediately after queuing the text.</p>
     * @param text the text to read out.
     * @return whether the text was successfully queued for reading out, or not.
     */
    public boolean queueSpeech(String text){
        // Media-Player should lower volume:
        int focus_res = am.requestAudioFocus(
                afl, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        );
        // Talk:
        if (focus_res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            // Add the text to the queue:
            int queue_res = tts.speak(text, TextToSpeech.QUEUE_ADD, this.tts_params);
            if (queue_res == TextToSpeech.SUCCESS){
                // Successfully queued:
                this.speech_count++;
                return true;
            }
        }
        return false;
    }

}
