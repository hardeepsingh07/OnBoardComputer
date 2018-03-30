package com.example.hardeepsingh.onboardcomputer.speech;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.example.hardeepsingh.onboardcomputer.utils.SpeechUtil;

import java.util.Locale;

/**
 * Text to Speech Helper class
 *
 * @author hardeepsingh on March 29,2018
 */
public class TextToSpeechConverter {

    private ConversionDelegate conversionDelegate;
    private Activity activity;
    private TextToSpeech textToSpeech;
    private String utteranceId = this.hashCode() + "";

    public TextToSpeechConverter(ConversionDelegate conversionDelegate) {
        this.conversionDelegate = conversionDelegate;
        this.activity = (Activity) conversionDelegate;
    }

    /**
     * Speak Message and Attach Progress Callback
     * @param message
     * @param speechDialogType
     * @return
     */
    public TextToSpeechConverter speakMessage(final String message, final SpeechDialogType speechDialogType) {
        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setPitch(1.3f);
                    textToSpeech.setSpeechRate(1f);
                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                    textToSpeech.setOnUtteranceProgressListener(new DeliveryProgressHandler(speechDialogType));
                } else {
                    conversionDelegate.onErrorOccurred(SpeechUtil.FAILED_TO_INITILIZE_TTS_ENGINE);
                }
            }
        });

        return this;
    }

    /**
     * Finish Speeching the Message and Shutdown
     */
    public void finish() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    /**
     * Hanlde UtteranceProgressListener Callbacks
     */
    public class DeliveryProgressHandler extends UtteranceProgressListener {

        private SpeechDialogType speechDialogType;

        public DeliveryProgressHandler(SpeechDialogType speechDialogType) {
            this.speechDialogType = speechDialogType;
        }

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            conversionDelegate.onCompletion(speechDialogType);
        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}
