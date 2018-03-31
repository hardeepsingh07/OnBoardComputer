package com.example.hardeepsingh.onboardcomputer.pathHandlers;

import org.json.JSONObject;

/**
 * Response Callbacks for GM Path Data and Serve Data
 *
 * @author hardeepsingh on March 30,2018
 */
public interface ResponseInterface {
    void onDataReceived(JSONObject jsonObject);
}
