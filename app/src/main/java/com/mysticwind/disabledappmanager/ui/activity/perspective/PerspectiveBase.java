package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.config.view.ViewOptionConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.ui.activity.help.HelpActivity;
import com.mysticwind.disabledappmanager.ui.activity.settings.SettingsActivity_;

import static java8.util.stream.StreamSupport.stream;

public abstract class PerspectiveBase extends AppCompatActivity {

    protected PackageListProvider packageListProvider;
    protected AppAssetUpdateEventManager appAssetUpdateEventManager;
    protected PackageStateController packageStateController;
    protected AppStateProvider appStateProvider;
    protected AppLauncher appLauncher;
    protected ManualStateUpdateEventManager manualStateUpdateEventManager;
    protected AppGroupManager appGroupManager;
    protected AppGroupUpdateEventManager appGroupUpdateEventManager;
    protected PackageStateUpdateEventManager packageStateUpdateEventManager;
    protected Drawable searchIconDrawable;
    protected Drawable closeIconDrawable;
    protected PackageAssetService packageAssetService;
    protected ViewOptionConfigDataAccessor viewOptionConfigDataAccessor;

    // search
    protected MenuItem searchAction;
    protected boolean searchBarDisplayed = false;
    protected EditText searchEditText;
    protected boolean showSystemApps = false;

    protected abstract void performSearch(String searchQuery);
    protected abstract void cancelSearch();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.packageListProvider = ApplicationHelper.from(this).packageListProvider();
        this.appAssetUpdateEventManager = ApplicationHelper.from(this).appAssetUpdateEventManager();
        this.packageStateController = ApplicationHelper.from(this).packageStateController();
        this.appStateProvider = ApplicationHelper.from(this).appStateProvider();
        this.appLauncher = ApplicationHelper.from(this).appLauncher();
        this.manualStateUpdateEventManager = ApplicationHelper.from(this).manualStateUpdateEventManager();
        this.appGroupManager = ApplicationHelper.from(this).appGroupManager();
        this.appGroupUpdateEventManager = ApplicationHelper.from(this).appGroupUpdateEventManager();
        this.packageStateUpdateEventManager = ApplicationHelper.from(this).packageStateUpdateEventManager();
        this.packageAssetService = ApplicationHelper.from(this).packageAssetService();
        this.viewOptionConfigDataAccessor = ApplicationHelper.from(this).viewOptionConfigDataAccessor();

        this.searchIconDrawable = getResources().getDrawable(R.drawable.ic_search_white_48dp);
        this.closeIconDrawable = getResources().getDrawable(R.drawable.ic_close_white_48dp);

        this.showSystemApps = viewOptionConfigDataAccessor.showSystemApps();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.showSystemApps = viewOptionConfigDataAccessor.showSystemApps();
    }

    protected void preloadPackageAssets() {
        stream(packageListProvider.getPackages(applicationFilter()))
                .forEach(
                        appInfo ->
                                packageAssetService.getPackageAssets(appInfo.getPackageName())
                );
    }

    protected ApplicationFilter applicationFilter() {
        return ApplicationFilter.builder()
                .includeSystemApp(this.showSystemApps)
                .build();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        searchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_search:
                if (searchBarDisplayed) {
                    closeSearchBar();
                } else {
                    openSearchBar();
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity_.class));
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSearchBar() {
        // Set custom view on action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search_bar);

        // Search edit text field setup.
        searchEditText = (EditText) actionBar.getCustomView().findViewById(R.id.search_query);
        searchEditText.addTextChangedListener(new SearchWatcher());
        searchEditText.setText(searchEditText.getText());
        searchEditText.requestFocus();

        // Change search icon accordingly.
        searchAction.setIcon(closeIconDrawable);
        searchBarDisplayed = true;

        performSearch(searchEditText.getText().toString());
    }

    private void closeSearchBar() {
        // Remove custom view.
        getSupportActionBar().setDisplayShowCustomEnabled(false);

        // Change search icon accordingly.
        searchAction.setIcon(searchIconDrawable);
        searchBarDisplayed = false;
        cancelSearch();
    }

    /**
     * Responsible for handling changes in search edit text.
     */
    private class SearchWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String searchQuery = searchEditText.getText().toString();
            performSearch(searchQuery);
        }
    }
}
