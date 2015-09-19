package com.mysticwind.disabledappmanager.domain;

import android.graphics.drawable.Drawable;

public interface AppIconProvider {
    Drawable getAppIcon(String packageName);
}
