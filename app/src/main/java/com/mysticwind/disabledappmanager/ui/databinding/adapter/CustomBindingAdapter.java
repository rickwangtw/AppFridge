package com.mysticwind.disabledappmanager.ui.databinding.adapter;

import android.databinding.BindingAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModelList;

import java.util.List;

import java8.util.stream.Collectors;

import static java8.util.stream.StreamSupport.stream;

public class CustomBindingAdapter {

    @BindingAdapter("bind:applications")
    public  static void bindApplicationList(final RecyclerView view,
                                            final ApplicationModelList applicationModelList) {

        List<ApplicationModel> filteredList = stream(applicationModelList.getApplicationModelList())
                .filter(applicationModel -> !applicationModel.isHidden())
                .collect(Collectors.toList());

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        view.setLayoutManager(layoutManager);
        view.setAdapter(new ApplicationListAdapter(filteredList));
    }
}
