package com.example.hardeepsingh.onboardcomputer.speech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.example.hardeepsingh.onboardcomputer.utils.SpeechUtil;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Speech To Text Convertor Helper Class
 *
 * @author hardeepsingh on March 29,2018
 */
public class SpeechToTextConverter {

    private static ConversionDelegate conversionDelegate;
    private Activity activity;

    public SpeechToTextConverter(ConversionDelegate conversionDelegate) {
        this.conversionDelegate = conversionDelegate;
        this.activity = (Activity) conversionDelegate;
    }

    /**
     * Start Speech Recognition Dialog
     * @param requestCode
     */
    public void speechWithDialog(int requestCode) {
        activity.startActivityForResult(getSpeechIntent(), requestCode);
    }


    /**
     * Listen without Dialog for specified SpeechRecognizer
     *
     * @param speechRecognizer
     */
    public void startListening(final SpeechRecognizer speechRecognizer) {
        if (speechRecognizer != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.startListening(getSpeechIntent());
                }
            });
        }
    }

    /**
     * Stop Listening for specified SpeechRecognizer
     * @param speechRecognizer
     */
    public void stopListening(final SpeechRecognizer speechRecognizer) {
        if (speechRecognizer != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.stopListening();
                }
            });
        }
    }

    /**
     * Generate Speech Recognization Intent to start service or dialog activity
     * @return
     */
    public Intent getSpeechIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Desired Building Number");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        return intent;
    }


    /**
     * Handle Recognition Listener Callbacks
     */
    public static class CustomRecognitionListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.e(TAG, "error " + error);
            conversionDelegate.onErrorOccurred(SpeechUtil.getErrorText(error));
        }

        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            conversionDelegate.onSuccess(data);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }


}
