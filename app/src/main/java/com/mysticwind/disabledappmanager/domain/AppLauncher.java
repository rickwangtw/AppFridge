package com.mysticwind.disabledappmanager.domain;

import android.content.Context;

public interface AppLauncher {
    void launch(Context context, String packageName);
}
