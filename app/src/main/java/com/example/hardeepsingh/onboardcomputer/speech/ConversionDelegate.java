package com.example.hardeepsingh.onboardcomputer.speech;

import java.util.ArrayList;

/**
 * Provide Results Callback from Speech To Text or Text to Speech Implementation
 *
 * @author Hardeep Singh (hardeepsingh@cpp.edu)
 * December 28,2018
 */
public interface ConversionDelegate {

    void onSuccess(ArrayList<String> result);

    void onCompletion(SpeechDialogType speechDialogType);

    void onErrorOccurred(String errorMessage);
}
