package com.iyer.vinayaka.util;

import org.springframework.stereotype.Component;

import javafx.application.Platform;

@Component
public class ExitHandler {

    /**
     * Method to be called on application exit.
     * Perform necessary cleanup operations here.
     */
    public void onExit() {
        System.out.println("Application is exiting. Performing cleanup...");
    }
}
