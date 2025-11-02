package com.multifinance;

import android.app.Application;

import lombok.Getter;

public class App extends Application {
    @Getter
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
