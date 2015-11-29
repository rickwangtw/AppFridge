package com.mysticwind.disabledappmanager.domain.asset.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CachedPackageAssetsDAO extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "cached_package_assets";
    private static final String PACKAGE_NAME_COLUMN = "package_name";
    private static final String APP_NAME_COLUMN = "app_name";
    private static final String ICON_COLUMN = "icon";

    private static final String TABLE_CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    PACKAGE_NAME_COLUMN + " TEXT, " +
                    APP_NAME_COLUMN + " TEXT, " +
                    ICON_COLUMN + " BLOB, " +
                    "PRIMARY KEY (" + PACKAGE_NAME_COLUMN + ") " +
                    ");";

    private static final String GET_ALL_SQL = SQLiteQueryBuilder.buildQueryString(
            false, TABLE_NAME,
            new String[] {PACKAGE_NAME_COLUMN, APP_NAME_COLUMN, ICON_COLUMN},
            null, null, null, null, null);

    private static final String PACKAGE_NAME_WHERE_CLAUSE = PACKAGE_NAME_COLUMN + "=?";

    private enum WriteOperation {
        CREATE,
        UPDATE,
        CREATE_OR_UPDATE
    }

    public CachedPackageAssetsDAO(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void createOrUpdate(String packageName, String appName, Drawable iconDrawable) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PACKAGE_NAME_COLUMN, packageName);
        contentValues.put(APP_NAME_COLUMN, appName);
        try {
            Bitmap iconBitmap = getBitmapFromDrawable(iconDrawable);
            byte[] iconBytes = flattenBitmap(iconBitmap);
            contentValues.put(ICON_COLUMN, iconBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to flatten icon for package " + packageName, e);
        }
        writeEntry(packageName, contentValues, WriteOperation.CREATE_OR_UPDATE);
    }

    // referenced from http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap = null;
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress bitmap!", e);
        }
    }

    private void writeEntry(String packageName, ContentValues contentValues, WriteOperation writeOperation) {
        switch (writeOperation) {
            case CREATE:
                insertEntry(TABLE_NAME, contentValues);
                break;
            case UPDATE:
                updateEntry(TABLE_NAME, contentValues, packageName);
                break;
            case CREATE_OR_UPDATE:
                if (containsEntry(packageName)) {
                    writeEntry(packageName, contentValues, WriteOperation.UPDATE);
                } else {
                    writeEntry(packageName, contentValues, WriteOperation.CREATE);
                }
                break;
        }
    }

    private void insertEntry(String tableName, ContentValues contentValues) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.insert(tableName, null, contentValues);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void updateEntry(String tableName, ContentValues contentValues, String packageName) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.update(tableName, contentValues, PACKAGE_NAME_WHERE_CLAUSE, new String[]{ packageName });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private boolean containsEntry(String packageName) {
        long count = DatabaseUtils.queryNumEntries(getReadableDatabase(),
                TABLE_NAME, PACKAGE_NAME_WHERE_CLAUSE, new String[]{ packageName });
        if (count > 0) {
            return true;
        }
        return false;
    }

    public List<PackageAssets> getAllPackageAssets() {
        Cursor cursor = getReadableDatabase().rawQuery(GET_ALL_SQL, null);
        if (cursor == null) {
            return Collections.emptyList();
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return Collections.emptyList();
        }
        ImmutableList.Builder<PackageAssets> packageAssetsBuilder = new ImmutableList.Builder<>();
        do {
            String packageName = cursor.getString(0);
            String appName = cursor.getString(1);
            Drawable iconDrawable;
            try {
                byte[] iconBytes = cursor.getBlob(2);
                Bitmap iconBitmap = unflattenBitmap(iconBytes);
                iconDrawable = new BitmapDrawable(iconBitmap);
            } catch (Exception e) {
                log.warn("Failed to get icon drawable for package " + packageName, e);
                continue;
            }
            packageAssetsBuilder.add(new PackageAssets(packageName, appName, iconDrawable));
        } while (cursor.moveToNext());
        cursor.close();
        return packageAssetsBuilder.build();
    }

    private Bitmap unflattenBitmap(byte[] bitmapBytes) {
        try {
            return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert bytes to bitmap", e);
        }
    }
}
