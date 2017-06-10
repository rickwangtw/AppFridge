package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.daimajia.swipe.SwipeLayout;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupActivityBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupAppItemBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupGroupItemBinding;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.databinding.model.AppGroupViewModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;
import com.mysticwind.library.widget.listview.expandable.adapter.MultimapExpandableListAdapter;

import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

@Slf4j
public class AppGroupPerspectiveTutorial extends PerspectiveBase {

    private static final String SHOWCASE_ID = AppGroupPerspectiveTutorial.class.getSimpleName();
    private static final String PACKAGE_NAME_1 = "com.my.application.1";
    private static final String PACKAGE_NAME_2 = "com.my.application.2";

    private Drawable defaultIconStub;
    private String allAppGroupName = "";
    private ExpandableListView expandableListView;
    private MultimapExpandableListAdapter multimapExpandableListAdapter;

    private final Comparator<AppGroupViewModel> appGroupViewModelComparator =
            (appGroupViewModel1, appGroupViewModel2) -> {
                if (allAppGroupName.equals(appGroupViewModel1.getAppGroupName())) {
                    return 1;
                } else if (allAppGroupName.equals(appGroupViewModel2.getAppGroupName())) {
                    return -1;
                }
                return appGroupViewModel1.getAppGroupName().compareTo(appGroupViewModel2.getAppGroupName());
            };
    private final Comparator<ApplicationModel> applicationModelComparator =
            (applicationModel1, applicationModel2) ->
                applicationModel1.getPackageName().compareTo(applicationModel2.getPackageName());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!appStateConfigDataAccessor.shouldShowAppGroupPerspectiveTutorial()) {
            startActivity(new Intent(this, AppGroupPerspective_.class));
            finish();
        }

        this.defaultIconStub = ApplicationHelper.from(this).defaultIconStubDrawable();
        this.allAppGroupName = getResources().getString(R.string.generated_app_group_name_all);

        setupView();

        expandableListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                startShowCaseSequence();

                if (Build.VERSION.SDK_INT >= 16) {
                    expandableListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private void setupView() {
        final LayoutInflater layoutInflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        PerspectiveAppgroupActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_appgroup_activity);
        expandableListView = binding.appGroupListView;

        final Multimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultimap =
                buildApplicationGroupToApplicationModelMultimap();

        final MultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel> viewGenerator =
                new MultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel>() {
                    @Override
                    public View populateGroupView(AppGroupViewModel appGroupViewModel, View groupView, ViewGroup parent) {
                        if (groupView == null) {
                            groupView = layoutInflator.inflate(R.layout.perspective_appgroup_group_item, null);
                        }
                        PerspectiveAppgroupGroupItemBinding groupBinding = DataBindingUtil.bind(groupView);
                        groupBinding.setAppGroup(appGroupViewModel);
                        groupBinding.executePendingBindings();
                        return groupView;
                    }

                    @Override
                    public View populateChildView(AppGroupViewModel appGroupViewModel, ApplicationModel applicationModel, View childView, ViewGroup parent) {
                        if (childView == null) {
                            childView = layoutInflator.inflate(R.layout.perspective_appgroup_app_item, null);
                        }
                        final PerspectiveAppgroupAppItemBinding appBinding = DataBindingUtil.bind(childView);

                        appBinding.setAppGroupName(appGroupViewModel.getAppGroupName());
                        appBinding.setApplication(applicationModel);
                        appBinding.setIsWithinVirtualAppGroup(appGroupViewModel.isVirtualGroup());
                        appBinding.executePendingBindings();
                        return childView;
                    }
                };
        this.multimapExpandableListAdapter =
                new MultimapExpandableListAdapter<>(appGroupToApplicationModelMultimap,
                        appGroupViewModelComparator, applicationModelComparator, viewGenerator);
        binding.appGroupListView.setAdapter(this.multimapExpandableListAdapter);

        expandableListView.expandGroup(0);
        expandableListView.expandGroup(1);
    }

    private Multimap<AppGroupViewModel, ApplicationModel> buildApplicationGroupToApplicationModelMultimap() {
        final String appGroup1 = getString(R.string.app_group_perspective_tutorial_appgroup1);
        final String appGroup2 = getString(R.string.app_group_perspective_tutorial_appgroup2);
        final String appLabel1 = getString(R.string.app_group_perspective_tutorial_applabel1);
        final String appLabel2 = getString(R.string.app_group_perspective_tutorial_applabel2);

        Multimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultiMap = ArrayListMultimap.create();
        // collapsed
        appGroupToApplicationModelMultiMap.put(
                AppGroupViewModel.builder()
                        .appGroupName(appGroup1)
                        .isVirtualGroup(false)
                        .build(),
                ApplicationModel.builder()
                        .packageName(PACKAGE_NAME_1)
                        .applicationLabel(appLabel1)
                        .applicationIcon(defaultIconStub)
                        .isEnabled(true)
                        .build());
        // expanded
        appGroupToApplicationModelMultiMap.put(
                AppGroupViewModel.builder()
                        .appGroupName(appGroup2)
                        .isVirtualGroup(false)
                        .build(),
                ApplicationModel.builder()
                        .packageName(PACKAGE_NAME_2)
                        .applicationLabel(appLabel2)
                        .applicationIcon(defaultIconStub)
                        .isEnabled(true)
                        .isEnabled(true)
                        .build());
        // all
        appGroupToApplicationModelMultiMap.put(
                AppGroupViewModel.builder()
                        .appGroupName(allAppGroupName)
                        .isVirtualGroup(true)
                        .build(),
                ApplicationModel.builder()
                        .packageName(PACKAGE_NAME_1)
                        .applicationLabel(appLabel1)
                        .applicationIcon(defaultIconStub)
                        .isEnabled(true)
                        .isEnabled(true)
                        .build());
        appGroupToApplicationModelMultiMap.put(
                AppGroupViewModel.builder()
                        .appGroupName(allAppGroupName)
                        .isVirtualGroup(true)
                        .build(),
                ApplicationModel.builder()
                        .packageName(PACKAGE_NAME_2)
                        .applicationLabel(appLabel2)
                        .applicationIcon(defaultIconStub)
                        .isEnabled(true)
                        .isEnabled(true)
                        .build());
        return appGroupToApplicationModelMultiMap;
    }

    private void startShowCaseSequence() {
        final MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        final View firstAppGroupView = expandableListView.getChildAt(0);
        final View secondAppGroupView = expandableListView.getChildAt(2);
        final View appViewOfFirstAppGroup = expandableListView.getChildAt(1);
        final View appViewOfSecondAppGroup = expandableListView.getChildAt(3);

        sequence.addSequenceItem(
                firstAppGroupView.findViewById(R.id.app_group_name),
                getString(R.string.app_group_perspective_tutorial_app_group_title),
                getString(R.string.app_group_perspective_tutorial_app_group_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                firstAppGroupView.findViewById(R.id.app_group_name),
                getString(R.string.app_group_perspective_tutorial_swipe_group_title),
                getString(R.string.app_group_perspective_tutorial_swipe_group_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                appViewOfFirstAppGroup.findViewById(R.id.app_label_noswipe),
                getString(R.string.app_group_perspective_tutorial_group_actions_title),
                getString(R.string.app_group_perspective_tutorial_group_actions_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                appViewOfFirstAppGroup.findViewById(R.id.app_label_swipe),
                getString(R.string.app_group_perspective_tutorial_swipe_app_title),
                getString(R.string.app_group_perspective_tutorial_swipe_app_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                // invisible view
                appViewOfFirstAppGroup.findViewById(R.id.app_label_noswipe),
                getString(R.string.app_group_perspective_tutorial_app_actions_title),
                getString(R.string.app_group_perspective_tutorial_app_actions_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                appViewOfSecondAppGroup.findViewById(R.id.app_label_swipe),
                getString(R.string.perspective_tutorial_click_to_launch_title),
                getString(R.string.perspective_tutorial_click_to_launch_description),
                getString(R.string.tutorial_check));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        // invisible view
                        .setTarget(appViewOfFirstAppGroup.findViewById(R.id.app_label_noswipe))
                        .setTitleText(R.string.settings_tutorial_title)
                        .setContentText(R.string.settings_tutorial_description)
                        .setDismissText(R.string.tutorial_check)
                        .build()
        );

        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int sequenceIndex) {
                if (sequenceIndex == 1) {
                    SwipeLayout groupSwipeLayout = (SwipeLayout) firstAppGroupView;
                    groupSwipeLayout.open();
                } else if (sequenceIndex == 3) {
                    LinearLayout linearLayout = (LinearLayout) appViewOfFirstAppGroup;
                    SwipeLayout childSwipeLayout = (SwipeLayout) linearLayout.getChildAt(1);
                    childSwipeLayout.open();
                } else if (sequenceIndex == 6) {
                    appStateConfigDataAccessor.updateAppGroupPerspectiveTutorialShown();
                    startActivity(new Intent(AppGroupPerspectiveTutorial.this, AppGroupPerspective_.class));
                    finish();
                }
            }
        });
        sequence.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.perspective_appgroup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void performSearch(String searchQuery) {
    }

    @Override
    protected void cancelSearch() {
    }
}
