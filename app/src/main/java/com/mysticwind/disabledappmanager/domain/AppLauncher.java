package com.mysticwind.disabledappmanager.domain;

import android.content.Intent;

public interface AppLauncher {
    Intent getLaunchIntentForPackage(String packageName);
}
