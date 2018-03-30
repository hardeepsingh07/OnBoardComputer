package com.example.hardeepsingh.onboardcomputer.speech;

import java.util.ArrayList;

/**
 * Provide Results Callback from Speech To Text or Text to Speech Implementation
 *
 * @author hardeepsingh on March 29,2018
 */
public interface ConversionDelegate {

    void onSuccess(ArrayList<String> result);

    void onCompletion(SpeechDialogType speechDialogType);

    void onErrorOccurred(String errorMessage);
}
