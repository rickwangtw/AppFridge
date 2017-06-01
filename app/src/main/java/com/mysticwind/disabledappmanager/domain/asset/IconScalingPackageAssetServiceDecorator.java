package com.mysticwind.disabledappmanager.domain.asset;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IconScalingPackageAssetServiceDecorator implements PackageAssetService {

    private final PackageAssetService packageAssetService;
    private final Context context;
    private final float targetMaxPixels;

    public IconScalingPackageAssetServiceDecorator(final PackageAssetService packageAssetService,
                                                   final Context context,
                                                   final float targetDpValue) {
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.context = Preconditions.checkNotNull(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.targetMaxPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetDpValue, metrics);
    }

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        final PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        if (packageAssets == null) {
            return null;
        }
        final Drawable scaledIconDrawable = scaleIcon(packageName, packageAssets.getIconDrawable());

        return new PackageAssets(packageAssets.getPackageName(), packageAssets.getAppName(), scaledIconDrawable);
    }

    private Drawable scaleIcon(final String packageName, final Drawable iconDrawable) {
        if ((iconDrawable == null) || !(iconDrawable instanceof BitmapDrawable)) {
            log.warn(String.format("Package [%s] Drawable not of BitmapDrawable, is [%s] instead",
                    packageName, iconDrawable.getClass().getSimpleName()));
            return iconDrawable;
        }
        final Bitmap bitmap = ((BitmapDrawable)iconDrawable).getBitmap();

        final int originalWidth = iconDrawable.getIntrinsicWidth();
        final int originalHeight = iconDrawable.getIntrinsicHeight();
        if (originalWidth >= targetMaxPixels && originalHeight >= targetMaxPixels) {
            float scaleFactor = targetMaxPixels / Math.max(originalWidth, originalHeight);
            log.info(String.format("Package [%s] icon width [%d] height [%d] compressing with scale factor [%f]",
                    packageName, originalWidth, originalHeight, scaleFactor));
            int sizeX = Math.round(originalWidth * scaleFactor);
            int sizeY = Math.round(originalHeight * scaleFactor);

            Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);
            return new BitmapDrawable(context.getResources(), bitmapResized);
        } else {
            return iconDrawable;
        }
    }
}
