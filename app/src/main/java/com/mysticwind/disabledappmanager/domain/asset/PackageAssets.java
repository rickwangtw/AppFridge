package com.mysticwind.disabledappmanager.domain.asset;

import android.graphics.drawable.Drawable;

import lombok.Value;

@Value
public class PackageAssets {
    private String packageName;
    private String appName;
    private Drawable iconDrawable;
}
