package com.mysticwind.disabledappmanager.domain.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AppGroupDAO extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "app_groups";
    private static final String APP_GROUP_NAME_COLUMN = "app_group_name";
    private static final String PACKAGE_NAME_COLUMN = "package_name";
    private static final String TABLE_CREATE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    APP_GROUP_NAME_COLUMN + " TEXT, " +
                    PACKAGE_NAME_COLUMN + " TEXT);";

    private final String allAppGroupSql;
    private final String getPackagesOfAppGroupSql;

    public AppGroupDAO(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
        this.allAppGroupSql = SQLiteQueryBuilder.buildQueryString(true, TABLE_NAME,
                new String[]{ APP_GROUP_NAME_COLUMN }, null, null, null, null, null);
        this.getPackagesOfAppGroupSql = SQLiteQueryBuilder.buildQueryString(true, TABLE_NAME,
                new String[]{ PACKAGE_NAME_COLUMN },
                APP_GROUP_NAME_COLUMN + "=?", null, null, null, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public Set<String> getAllAppGroups() {
        Cursor cursor = getReadableDatabase().rawQuery(allAppGroupSql, null);
        return getStringSetFrom(cursor);
    }

    public Set<String> getPackagesOfAppGroup(String appGroupName) {
        Cursor cursor = getReadableDatabase()
                .rawQuery(getPackagesOfAppGroupSql, new String[]{appGroupName});
        return getStringSetFrom(cursor);
    }

    public void addPackagesToAppGroup(Collection<String> packageNames, String appGroupName) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            for (String packageName : packageNames) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PACKAGE_NAME_COLUMN, packageName);
                contentValues.put(APP_GROUP_NAME_COLUMN, appGroupName);
                db.insert(TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private Set<String> getStringSetFrom(Cursor cursor) {
        if (cursor == null) {
            return Collections.emptySet();
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return Collections.emptySet();
        }
        Set<String> itemSet = new HashSet<>();
        do {
            itemSet.add(cursor.getString(0));
        } while (cursor.moveToNext());
        cursor.close();

        return itemSet;
    }

    public void deleteAppGroup(String appGroupName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, APP_GROUP_NAME_COLUMN + "=?", new String[] { appGroupName });
        db.close();
    }

    public void deletePackageFromAppGroup(String packageName, String appGroupName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, PACKAGE_NAME_COLUMN + "=? and " + APP_GROUP_NAME_COLUMN + "=?",
                new String[] { packageName, appGroupName });
        db.close();
    }
}
