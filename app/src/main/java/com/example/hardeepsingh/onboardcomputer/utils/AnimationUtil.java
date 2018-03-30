package com.example.hardeepsingh.onboardcomputer.utils;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.hardeepsingh.onboardcomputer.databinding.ActivityWaypointRouteBinding;


/**
 * Animation Util Class to Animate Launch, Destination and Route Panels
 *
 * @author by hardeep.singh@mobileforming.com
 *         March 27, 2018
 */

public class AnimationUtil {

    private static float translationX;
    private static float translationY;

    /**
     * ActivityWaypointRouteBinding Layout Animation
     * @param binding
     */
    /**
     * Animate Launch Screen
     */
    public static void showLaunchPanel(final ActivityWaypointRouteBinding binding) {
        translationX = binding.destinationInfo.getTranslationX();

        //Check if Complete Panel is out
        if (binding.completePanel.getTranslationY() == 0) {
            binding.completePanel.animate().translationY(translationY).setDuration(350).withEndAction(new Runnable() {
                @Override
                public void run() {
                    showLaunchPanel(binding);
                }
            }).start();
        }
        // Check if InRoute Panel is Out
        else if (binding.inRoutePanel.getTranslationX() == 0) {
            binding.launchOptions.animate().scaleX(0).scaleX(0).setDuration(350).start();
            binding.inRoutePanel.animate().translationX(translationX).setStartDelay(175).setDuration(350).withEndAction(new Runnable() {
                @Override
                public void run() {
                    showLaunchPanel(binding);
                }
            }).start();
        } else {
            launchPanel(binding);
        }
    }

    public static void launchPanel(final ActivityWaypointRouteBinding binding) {
        binding.stopButton.setVisibility(View.GONE);
        binding.launchButton.setVisibility(View.VISIBLE);
        binding.launchOptions.animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator()).setDuration(500).setStartDelay(350).start();
        binding.destinationInfo.animate().translationX(0).setDuration(500).setInterpolator(new DecelerateInterpolator()).setStartDelay(500).start();
    }

    /**
     * Animate In-Route Screen
     */
    public static void showInRoutePanel(final ActivityWaypointRouteBinding binding) {
        //Transition Launch Options Button From LAUNCH To STOP
        binding.launchOptions.animate().scaleX(0).scaleY(0).setDuration(350).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.launchButton.setVisibility(View.GONE);
                binding.stopButton.setVisibility(View.VISIBLE);
                binding.launchOptions.animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator()).setDuration(500).start();
            }
        }).start();

        //Hide Destination Info Panel and Show InRoute Panel
        binding.destinationInfo.animate().translationX(translationX).setStartDelay(175).setDuration(350).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.inRoutePanel.animate().translationX(0).setDuration(500).setInterpolator(new DecelerateInterpolator()).setStartDelay(500).start();
            }
        }).start();
    }

    /**
     * Animate Destination Panel
     */
    public static void showDestinationPanel(final ActivityWaypointRouteBinding binding) {
        translationY = binding.completePanel.getTranslationY();

        //Shrink away STOP or LAUNCH Button
        binding.launchOptions.animate().scaleX(0).scaleX(0).setDuration(350).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.stopButton.setVisibility(View.GONE);
                binding.launchButton.setVisibility(View.GONE);
            }
        }).start();

        //Hide InRoutePanel and Show Destination Info Panel
        binding.inRoutePanel.animate().translationX(translationX).setStartDelay(175).setDuration(350).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.completePanel.animate().translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator()).setStartDelay(500).start();
            }
        }).start();
    }

}
