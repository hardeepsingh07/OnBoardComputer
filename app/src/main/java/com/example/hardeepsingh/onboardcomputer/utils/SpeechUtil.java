package com.example.hardeepsingh.onboardcomputer.utils;

import android.speech.SpeechRecognizer;

/**
 * Helper Methods for Speech Conversion
 *
 * @author Hardeep Singh (hardeepsingh@cpp.edu)
 * December 28,2018
 */
public class SpeechUtil {

    public static final String FILED_TO_INITIALIZE_STT_ENGINE = "Sorry! Your device doesn\'t support speech input";
    public static final String FAILED_TO_INITILIZE_TTS_ENGINE = "Filed to initialize TTS engine";

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}