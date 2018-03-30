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

    private ArrayList<String> data;
    private ConversionDelegate conversionDelegate;
    private SpeechRecognizer speechRecognizer;
    private Activity activity;

    public SpeechToTextConverter(ConversionDelegate conversionDelegate) {
        this.conversionDelegate = conversionDelegate;
        this.activity = (Activity) conversionDelegate;
    }

    public SpeechToTextConverter speechWithoutDialog() {
        CustomRecognitionListener listener = new CustomRecognitionListener();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(listener);
        return this;
    }

    public void speechWithDialog(int requestCode) {
        activity.startActivityForResult(getSpeechIntent(), requestCode);
    }


    public void startListening() {
        speechRecognizer.startListening(getSpeechIntent());
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public Intent getSpeechIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Desired Building Number");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        return intent;
    }

    class CustomRecognitionListener implements RecognitionListener {
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
            String str = new String();
            Log.d(TAG, "onResults " + results);
            data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
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
