package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.os.AsyncTask;
import android.os.Bundle;

import org.androidannotations.annotations.EActivity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class PackageStatePerspective extends PackageStatePerspectiveBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loadingDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                // loading application assets takes the most time
                preloadPackageAssets();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                setupView(false);

                loadingDialog.dismiss();
            }
        }.execute();
    }

}
