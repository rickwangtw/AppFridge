package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Spinner;

import com.google.common.collect.ImmutableList;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.BR;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateAppItemBinding;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java8.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class PackageStatePerspectiveTutorial extends PerspectiveBase {

    private static final String SHOWCASE_ID = PackageStatePerspectiveTutorial.class.getSimpleName();
    private static final String PACKAGE_NAME = "com.my.application";

    private static final int LAST_SEQUENCE_INDEX = 4;
    private static final int THREAD_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10_000;
    private static final long KEEP_ALIVE_IN_MINUTES = 5;

    protected Drawable defaultIconStub;

    private RecyclerView recyclerView;

    private ExecutorService threadPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_IN_MINUTES, TimeUnit.MINUTES, new ArrayBlockingQueue<>(MAX_POOL_SIZE));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!appStateConfigDataAccessor.shouldShowPackageStatePerspectiveTutorial()) {
            startActivity(new Intent(this, PackageStatePerspective_.class));
            finish();
        }

        this.defaultIconStub = ApplicationHelper.from(this).defaultIconStubDrawable();

        setContentView(R.layout.perspective_state_activity);

        recyclerView = (RecyclerView) findViewById(R.id.appListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RxDataSource<ApplicationModel> dataSource = new RxDataSource<>(
                ImmutableList.of(
                        ApplicationModel.builder()
                                .packageName(PACKAGE_NAME)
                                .applicationLabel(getString(R.string.package_state_perspective_tutorial_app_name))
                                .applicationIcon(defaultIconStub)
                                .isEnabled(true)
                                .selected(false)
                                .applicationLauncher(packageName -> {})
                                .build()
                )
        );
        dataSource
                .<PerspectiveStateAppItemBinding>bindRecyclerView(recyclerView, R.layout.perspective_state_app_item)
                .subscribe(viewHolder -> {
                    PerspectiveStateAppItemBinding itemBinding = viewHolder.getViewDataBinding();
                    ApplicationModel applicationModel = viewHolder.getItem();
                    itemBinding.setVariable(BR.application, applicationModel);
                    itemBinding.executePendingBindings();
                });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                startShowCaseSequence();

                if (Build.VERSION.SDK_INT >= 16) {
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<AppInfo> appInfoList = packageListProvider.getOrderedPackages(
                        ApplicationFilter.builder()
                                .includeSystemApp(true)
                                .build(),
                        ApplicationOrderingMethod.PACKAGE_NAME);

                // load assets for non-system apps
                loadPackageAssets(appInfoList, appInfo -> !appInfo.isSystemApp());

                // load assets for system apps
                loadPackageAssets(appInfoList, appInfo -> appInfo.isSystemApp());
                return null;
            }
        }.execute();
    }

    private void loadPackageAssets(final Collection<AppInfo> appInfos,
                                   final Predicate<AppInfo> appInfoPredicate) {
        stream(appInfos)
                .filter(appInfoPredicate)
                .forEach(appInfo ->
                        threadPool.submit(
                                () -> packageAssetService.getPackageAssets(appInfo.getPackageName()))
                );
    }

    private void startShowCaseSequence() {
        final MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        sequence.addSequenceItem(
                findViewById(R.id.add_to_group_button),
                getString(R.string.package_state_perspective_tutorial_add_to_group_button_title),
                getString(R.string.package_state_perspective_tutorial_add_to_group_button_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                findViewById(R.id.disable_app_button),
                getString(R.string.package_state_perspective_tutorial_disable_app_button_title),
                getString(R.string.package_state_perspective_tutorial_disable_app_button_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                findViewById(R.id.app_status_spinner),
                getString(R.string.package_state_perspective_tutorial_app_status_spinner_title),
                getString(R.string.package_state_perspective_tutorial_app_status_spinner_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                recyclerView.getChildAt(0).findViewById(R.id.packagename),
                getString(R.string.perspective_tutorial_click_to_launch_title),
                getString(R.string.perspective_tutorial_click_to_launch_description),
                getString(R.string.tutorial_check));

        MaterialShowcaseView settingsShowcaseView = new MaterialShowcaseView.Builder(this)
                .setTarget(recyclerView.getChildAt(0).findViewById(R.id.packagename))
                .setTitleText(R.string.settings_tutorial_title)
                .setContentText(R.string.settings_tutorial_description)
                .withoutShape()
                .setDismissText(R.string.tutorial_check)
                .build();
        sequence.addSequenceItem(settingsShowcaseView);

        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int sequenceIndex) {
                if (sequenceIndex == 1) {
                    Spinner spinner = (Spinner) findViewById(R.id.app_status_spinner);
                    spinner.performClick();
                } else if (sequenceIndex == LAST_SEQUENCE_INDEX) {
                    appStateConfigDataAccessor.updatePackageStatePerspectiveTutorialShown();
                    startActivity(new Intent(PackageStatePerspectiveTutorial.this, FirstLaunchOptimizedPackageStatePerspective_.class));
                    finish();
                }
            }
        });

        sequence.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.perspective_state_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void performSearch(String searchQuery) {
    }

    @Override
    protected void cancelSearch() {
    }
}
