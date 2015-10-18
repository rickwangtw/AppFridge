package com.mysticwind.disabledappmanager.common;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;

import com.mysticwind.disabledappmanager.AppFridgeApplication;

public class ApplicationHelper {
    public static AppFridgeApplication from(Activity activity) {
        return from(activity.getApplication());
    }

    public static AppFridgeApplication from(Fragment fragment) {
        return from(fragment.getActivity().getApplication());
    }

    public static AppFridgeApplication from(Service service) {
        return from(service.getApplication());
    }

    public static AppFridgeApplication from(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (! (applicationContext instanceof AppFridgeApplication)) {
            throw new RuntimeException("Application context (" + applicationContext.getClass().getSimpleName() + ") not instance of AppFridgeApplication");
        }
        return (AppFridgeApplication) applicationContext;
    }

    public static AppFridgeApplication from(Application application) {
        return (AppFridgeApplication)application;
    }
}
